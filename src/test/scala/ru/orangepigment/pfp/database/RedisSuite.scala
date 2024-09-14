package ru.orangepigment.pfp.database

import scala.concurrent.duration.DurationInt

import cats.effect.kernel.Ref
import cats.effect.{ IO, Resource }
import cats.syntax.eq._
import dev.profunktor.redis4cats.log4cats._
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import ru.orangepigment.pfp.models.{
  Brand,
  BrandName,
  Cart,
  Category,
  CategoryName,
  CreateItem,
  ID,
  Item,
  ItemId,
  ShoppingCartExpiration,
  UpdateItem
}
import ru.orangepigment.pfp.services.{ Items, ShoppingCart }
import ru.orangepigment.pfp.utils.ResourceSuite
import ru.orangepigment.pfp.utils.generators.{ itemGen, quantityGen, userIdGen }

object RedisSuite extends ResourceSuite {
  given Logger[IO] = NoOpLogger[IO]

  type Res = RedisCommands[IO, String, String]

  override def sharedResource: Resource[IO, Res] =
    Redis[IO]
      .utf8("redis://localhost")
      .beforeAll(_.flushAll)

  val expiration = ShoppingCartExpiration(30.seconds)
  /*val tokenConfig = JwtSecretKeyConfig("bar")
  val tokenExp = TokenExpiration(30.seconds)
  val jwtClaim = JwtClaim("test")
  val userJwtAuth = UserJwtAuth(
    JwtAuth.hmac("bar", JwtAlgorithm.HS256)
  )*/

  test("Shopping Cart") { redis =>
    val gen = for {
      uid <- userIdGen
      it1 <- itemGen
      it2 <- itemGen
      q1  <- quantityGen
      q2  <- quantityGen
    } yield (uid, it1, it2, q1, q2)

    forall(gen) { case (uid, it1, it2, q1, q2) =>
      Ref.of[IO, Map[ItemId, Item]](Map(it1.uuid -> it1, it2.uuid -> it2)).flatMap { ref =>
        val items = new TestItems(ref)
        val c     = ShoppingCart.make[IO](items, redis, expiration)
        for {
          x <- c.get(uid)
          _ <- c.add(uid, it1.uuid, q1)
          _ <- c.add(uid, it2.uuid, q1)
          y <- c.get(uid)
          _ <- c.removeItem(uid, it1.uuid)
          z <- c.get(uid)
          _ <- c.update(uid, Cart(Map(it2.uuid -> q2)))
          w <- c.get(uid)
          _ <- c.delete(uid)
          v <- c.get(uid)
        } yield expect.all(
          x.items.isEmpty,
          y.items.size === 2,
          z.items.size === 1,
          v.items.isEmpty,
          w.items.headOption.fold(false)(_.quantity === q2)
        )
      }
    }
  }

  protected class TestItems(
      ref: Ref[IO, Map[ItemId, Item]]
  ) extends Items[IO] {
    def findAll: IO[List[Item]] =
      ref.get.map(_.values.toList)

    def findBy(brand: BrandName): IO[List[Item]] =
      ref.get.map {
        _.values.filter(_.brand.name === brand).toList
      }

    def findById(itemId: ItemId): IO[Option[Item]] =
      ref.get.map(_.get(itemId))

    def create(item: CreateItem): IO[ItemId] =
      ID.make[IO, ItemId].flatTap { id =>
        val brand    = Brand(item.brandId, BrandName("foo"))
        val category = Category(item.categoryId, CategoryName("foo"))
        val newItem = Item(
          id,
          item.name,
          item.description,
          item.price,
          brand,
          category
        )
        ref.update(_.updated(id, newItem))
      }

    def update(item: UpdateItem): IO[Unit] =
      ref.update { x =>
        x.get(item.id).fold(x) { i =>
          x.updated(item.id, i.copy(price = item.price))
        }
      }
  }

}
