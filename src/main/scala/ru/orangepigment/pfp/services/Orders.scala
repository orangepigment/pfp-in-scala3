package ru.orangepigment.pfp.services

import cats.data.NonEmptyList
import cats.effect.{ Concurrent, Resource }
import cats.syntax.functor.*
import cats.syntax.flatMap.*
import ru.orangepigment.pfp.models.{ CartItem, ID, ItemId, Order, OrderId, PaymentId, Quantity, UserId }
import squants.market.Money
import ru.orangepigment.pfp.util.GenUUID
import ru.orangepigment.pfp.util.sqlcodecs.*
import skunk.*
import skunk.circe.codec.all.*
import skunk.implicits.*

trait Orders[F[_]] {
  def get(
      userId: UserId,
      orderId: OrderId
  ): F[Option[Order]]

  def findBy(userId: UserId): F[List[Order]]

  def create(
      userId: UserId,
      paymentId: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId]
}

object Orders {
  def make[F[_]: Concurrent: GenUUID](
      postgres: Resource[F, Session[F]]
  ): Orders[F] =
    new Orders[F] {
      import OrderSQL._

      def get(userId: UserId, orderId: OrderId): F[Option[Order]] =
        postgres.use { session =>
          session.prepare(selectByUserIdAndOrderId).flatMap { q =>
            q.option(userId -> orderId)
          }
        }

      def findBy(userId: UserId): F[List[Order]] =
        postgres.use { session =>
          session.prepare(selectByUserId).flatMap { q =>
            q.stream(userId, 1024).compile.toList
          }
        }

      def create(
          userId: UserId,
          paymentId: PaymentId,
          items: NonEmptyList[CartItem],
          total: Money
      ): F[OrderId] =
        postgres.use { session =>
          session.prepare(insertOrder).flatMap { cmd =>
            ID.make[F, OrderId].flatMap { id =>
              val itMap = items.toList.map(x => x.item.uuid -> x.quantity).toMap
              val order = Order(id, paymentId, itMap, total)
              cmd.execute(userId -> order).as(id)
            }
          }
        }

    }
}

private object OrderSQL {
  val decoder: Decoder[Order] =
    (
      orderId ~ userId ~ paymentId ~
        jsonb[Map[ItemId, Quantity]] ~ money
    ).map { case o ~ _ ~ p ~ i ~ t =>
      Order(o, p, i, t)
    }

  val selectByUserId: Query[UserId, Order] =
    sql"""
        SELECT * FROM orders
        WHERE user_id = $userId
       """.query(decoder)

  val selectByUserIdAndOrderId: Query[UserId ~ OrderId, Order] =
    sql"""
        SELECT * FROM orders
        WHERE user_id = $userId
        AND uuid = $orderId
       """.query(decoder)

  val encoder: Encoder[UserId ~ Order] =
    (
      orderId ~ userId ~ paymentId ~
        jsonb[Map[ItemId, Quantity]] ~ money
    ).contramap { case id ~ o =>
      o.id -> id -> o.paymentId -> o.items -> o.total
    }

  val insertOrder: Command[UserId ~ Order] =
    sql"""
        INSERT INTO orders
        VALUES ($encoder)
       """.command
}
