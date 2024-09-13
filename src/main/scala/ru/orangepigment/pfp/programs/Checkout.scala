package ru.orangepigment.pfp.programs

import scala.concurrent.duration.DurationInt

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.syntax.apply._
import cats.syntax.applicativeError._
import cats.syntax.monadError._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.show._
import org.typelevel.log4cats.Logger
import retry.RetryPolicy
import ru.orangepigment.pfp.clients.PaymentClient
import ru.orangepigment.pfp.models.Errors.{ EmptyCartError, OrderError, PaymentError }
import ru.orangepigment.pfp.models.{ Card, CartItem, CartTotal, OrderId, Payment, PaymentId, UserId }
import ru.orangepigment.pfp.services.{ Orders, ShoppingCart }
import ru.orangepigment.pfp.util.Background
import ru.orangepigment.pfp.util.retries.{ Retriable, Retry }
import squants.market.Money

final class Checkout[F[_]: Background: Logger: MonadThrow: Retry](
    payments: PaymentClient[F],
    cart: ShoppingCart[F],
    orders: Orders[F],
    policy: RetryPolicy[F]
) {

  def process(userId: UserId, card: Card): F[OrderId] =
    cart.get(userId).flatMap { case CartTotal(items, total) =>
      for {
        its <- ensureNonEmpty(items)
        pid <- processPayment(Payment(userId, total, card))
        oid <- createOrder(userId, pid, its, total)
        _   <- cart.delete(userId).attempt.void
      } yield oid
    }

  private def ensureNonEmpty[A](xs: List[A]): F[NonEmptyList[A]] =
    MonadThrow[F].fromOption(
      NonEmptyList.fromList(xs),
      EmptyCartError
    )

  private def processPayment(in: Payment): F[PaymentId] =
    Retry[F]
      .retry(policy, Retriable.Payments)(payments.process(in))
      .adaptError { case e =>
        PaymentError(
          Option(e.getMessage).getOrElse("Unknown")
        )
      }

  private def createOrder(
      userId: UserId,
      paymentId: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId] = {
    val action =
      Retry[F]
        .retry(policy, Retriable.Orders)(
          orders.create(userId, paymentId, items, total)
        )
        .adaptError { case e =>
          OrderError(e.getMessage)
        }

    def bgAction(fa: F[OrderId]): F[OrderId] =
      fa.onError { case_ =>
        Logger[F].error(s"Failed to create order for: ${paymentId.show}") *>
          Background[F].schedule(bgAction(fa), 1.hour)
      }

    bgAction(action)
  }
}
