package ru.orangepigment.pfp.services

import cats.effect.{ MonadCancelThrow, Resource }
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import ru.orangepigment.pfp.models.{ Category, CategoryId, CategoryName, ID }
import ru.orangepigment.pfp.util.GenUUID
import ru.orangepigment.pfp.util.sqlcodecs.*
import skunk.*
import skunk.implicits.*

trait Categories[F[_]] {
  def findAll: F[List[Category]]

  def create(name: CategoryName): F[CategoryId]
}

object Categories {
  def make[F[_]: GenUUID: MonadCancelThrow](
      postgres: Resource[F, Session[F]]
  ): Categories[F] =
    new Categories[F] {

      import CategorySQL._

      def findAll: F[List[Category]] =
        postgres.use(_.execute(selectAll))

      def create(name: CategoryName): F[CategoryId] =
        postgres.use { session =>
          session.prepare(insertCategory).flatMap { cmd =>
            ID.make[F, CategoryId].flatMap { id =>
              cmd.execute(Category(id, name)).as(id)
            }
          }
        }
    }
}

private object CategorySQL {
  val codec: Codec[Category] =
    (categoryId ~ categoryName).imap { case i ~ n =>
      Category(i, n)
    }(b => (b.uuid, b.name))

  val selectAll: Query[Void, Category] =
    sql"""
      SELECT * FROM Categories
    """.query(codec)

  val insertCategory: Command[Category] =
    sql"""
    INSERT INTO Categories
    VALUES($codec)
    """.command

}
