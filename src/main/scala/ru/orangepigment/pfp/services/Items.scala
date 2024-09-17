package ru.orangepigment.pfp.services

import cats.effect.{ Concurrent, Resource }
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import ru.orangepigment.pfp.models.{ Brand, BrandName, Category, CreateItem, ID, Item, ItemId, UpdateItem }
import ru.orangepigment.pfp.util.GenUUID
import ru.orangepigment.pfp.util.sqlcodecs.*
import skunk.*
import skunk.implicits.*

trait Items[F[_]] {
  def findAll: F[List[Item]]

  def findBy(brand: BrandName): F[List[Item]]

  def findById(itemId: ItemId): F[Option[Item]]

  def create(item: CreateItem): F[ItemId]

  def update(item: UpdateItem): F[Unit]
}

object Items {

  def make[F[_]: Concurrent: GenUUID](
      postgres: Resource[F, Session[F]]
  ): Items[F] =
    new Items[F] {

      import ItemSQL.*

      def findAll: F[List[Item]] =
        postgres.use(_.execute(selectAll))

      def findBy(brand: BrandName): F[List[Item]] =
        postgres.use { session =>
          session.prepare(selectByBrand).flatMap { ps =>
            ps.stream(brand, 1024).compile.toList
          }
        }

      def findById(itemId: ItemId): F[Option[Item]] =
        postgres.use { session =>
          session.prepare(selectById).flatMap { ps =>
            ps.option(itemId)
          }
        }

      def create(item: CreateItem): F[ItemId] =
        postgres.use { session =>
          session.prepare(insertItem).flatMap { cmd =>
            ID.make[F, ItemId].flatMap { id =>
              cmd.execute(id, item).as(id)
            }
          }
        }

      def update(item: UpdateItem): F[Unit] =
        postgres.use { session =>
          session.prepare(updateItem).flatMap { cmd =>
            cmd.execute(item).void
          }
        }
    }
}

private object ItemSQL {
  val decoder: Decoder[Item] =
    (
      itemId ~ itemName ~ itemDesc ~ money ~ brandId ~
        brandName ~ categoryId ~ categoryName
    ).map { case i ~ n ~ d ~ p ~ bi ~ bn ~ ci ~ cn =>
      Item(i, n, d, p, Brand(bi, bn), Category(ci, cn))
    }

  val selectAll: Query[Void, Item] =
    sql"""
          SELECT i.uuid, i.name, i.description, i.price,
                 b.uuid, b.name, c.uuid, c.name
          FROM items AS i
          INNER JOIN brands AS b ON i.brand_id = b.uuid
          INNER JOIN categories AS c ON i.category_id = c.uuid
         """.query(decoder)

  val selectByBrand: Query[BrandName, Item] =
    sql"""
        SELECT i.uuid, i.name, i.description, i.price,
               b.uuid, b.name, c.uuid, c.name
        FROM items AS i
        INNER JOIN brands AS b ON i.brand_id = b.uuid
        INNER JOIN categories AS c ON i.category_id = c.uuid
        WHERE b.name LIKE $brandName
    """.query(decoder)

  val selectById: Query[ItemId, Item] =
    sql"""
        SELECT i.uuid, i.name, i.description, i.price,
               b.uuid, b.name, c.uuid, c.name
        FROM items AS i
        INNER JOIN brands AS b ON i.brand_id = b.uuid
        INNER JOIN categories AS c ON i.category_id = c.uuid
        WHERE i.uuid = $itemId
       """.query(decoder)

  val insertItem: Command[ItemId ~ CreateItem] =
    sql"""
        INSERT INTO items
        VALUES (
          $itemId, $itemName, $itemDesc,
          $money, $brandId, $categoryId
        )
    """.command.contramap { case id ~ i =>
      (id, i.name, i.description, i.price, i.brandId, i.categoryId)
    }

  val updateItem: Command[UpdateItem] =
    sql"""
        UPDATE items
        SET price = $money
        WHERE uuid = ${itemId}
       """.command.contramap { i =>
      (i.price, i.id)
    }
}
