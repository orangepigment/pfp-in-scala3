package ru.orangepigment.pfp.programs

import scala.concurrent.duration.*
import scala.util.control.NoStackTrace

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.kernel.Ref
import cats.syntax.apply.*
import cats.syntax.eq.*
import cats.syntax.semigroup.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import retry.RetryDetails.{ GivingUp, WillDelayAndRetry }
import retry.RetryPolicies.limitRetries
import retry.RetryPolicy
import ru.orangepigment.pfp.clients.PaymentClient
import ru.orangepigment.pfp.models.*
import ru.orangepigment.pfp.models.Errors.{ EmptyCartError, OrderError, PaymentError }
import ru.orangepigment.pfp.services.{ Orders, ShoppingCart }
import ru.orangepigment.pfp.util.Background
import ru.orangepigment.pfp.util.retries.Retry
import ru.orangepigment.pfp.utils.{ TestBackground, TestRetry }
import ru.orangepigment.pfp.utils.generators.*
import squants.market.{ Money, USD }
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object CheckoutSuite extends SimpleIOSuite with Checkers {

  given Background[IO] = TestBackground.NoOp
  given Logger[IO]     = NoOpLogger[IO]

  def successfulClient(pid: PaymentId): PaymentClient[IO] =
    new PaymentClient[IO] {
      def process(payment: Payment): IO[PaymentId] =
        IO.pure(pid)
    }

  val unreachableClient: PaymentClient[IO] =
    new PaymentClient[IO] {
      def process(payment: Payment): IO[PaymentId] =
        IO.raiseError(PaymentError(""))
    }

  def recoveringClient(
      attemptsSoFar: Ref[IO, Int],
      paymentId: PaymentId
  ): PaymentClient[IO] =
    new PaymentClient[IO] {
      def process(payment: Payment): IO[PaymentId] =
        attemptsSoFar.get.flatMap {
          case n if n === 1 => IO.pure(paymentId)
          case _ =>
            attemptsSoFar.update(_ + 1) *>
              IO.raiseError(PaymentError(""))
        }
    }

  def successfulCart(cartTotal: CartTotal): ShoppingCart[IO] =
    new TestCart {
      override def get(userId: UserId): IO[CartTotal] =
        IO.pure(cartTotal)

      override def delete(userId: UserId): IO[Unit] =
        IO.unit
    }

  val emptyCart: ShoppingCart[IO] =
    new TestCart {
      override def get(userId: UserId): IO[CartTotal] =
        IO.pure(CartTotal(List.empty, USD(0)))
    }

  def failingCart(cartTotal: CartTotal): ShoppingCart[IO] =
    new TestCart {
      override def get(userId: UserId): IO[CartTotal] =
        IO.pure(cartTotal)

      override def delete(userId: UserId): IO[Unit] =
        IO.raiseError(new NoStackTrace {})
    }

  def successfulOrders(oid: OrderId): Orders[IO] =
    new TestOrders {
      override def create(
          userId: UserId,
          paymentId: PaymentId,
          items: NonEmptyList[CartItem],
          total: Money
      ): IO[OrderId] =
        IO.pure(oid)
    }

  val failingOrders: Orders[IO] =
    new TestOrders {
      override def create(
          userId: UserId,
          paymentId: PaymentId,
          items: NonEmptyList[CartItem],
          total: Money
      ): IO[OrderId] =
        IO.raiseError(OrderError(""))
    }

  val MaxRetries = 3
  val retryPolicy: RetryPolicy[IO] =
    limitRetries[IO](MaxRetries)

  val gen = for {
    uid <- userIdGen
    pid <- paymentIdGen
    oid <- orderIdGen
    crt <- cartTotalGen
    crd <- cardGen
  } yield (uid, pid, oid, crt, crd)

  test("successful checkout") {
    forall(gen) { case (uid, pid, oid, ct, card) =>
      Checkout[IO](
        successfulClient(pid),
        successfulCart(ct),
        successfulOrders(oid),
        retryPolicy
      ).process(uid, card).map(expect.same(oid, _))
    }
  }

  test("empty cart") {
    forall(gen) { case (uid, pid, oid, _, card) =>
      Checkout[IO](
        successfulClient(pid),
        emptyCart,
        successfulOrders(oid),
        retryPolicy
      ).process(uid, card)
        .attempt
        .map {
          case Left(EmptyCartError) =>
            success
          case _ =>
            failure("Cart was not empty as expected")
        }
    }
  }

  test("unreachable payment client") {
    forall(gen) { case (uid, _, oid, ct, card) =>
      Ref.of[IO, Option[GivingUp]](None).flatMap { retries =>
        given Retry[IO] = TestRetry.givingUp(retries)
        Checkout[IO](
          unreachableClient,
          successfulCart(ct),
          successfulOrders(oid),
          retryPolicy
        ).process(uid, card)
          .attempt
          .flatMap {
            case Left(PaymentError(_)) =>
              retries.get.map {
                case Some(g) =>
                  expect.same(g.totalRetries, MaxRetries)
                case None =>
                  failure("expected GivingUp")
              }
            case _ =>
              IO.pure(failure("Expected payment error"))
          }
      }
    }
  }

  test("failing payment client succeeds after one retry") {
    forall(gen) { case (uid, pid, oid, ct, card) =>
      (Ref.of[IO, Option[WillDelayAndRetry]](None), Ref.of[IO, Int](0)).tupled.flatMap { case (retries, cliRef) =>
        given Retry[IO] = TestRetry.recovering(retries)
        Checkout[IO](
          recoveringClient(cliRef, pid),
          successfulCart(ct),
          successfulOrders(oid),
          retryPolicy
        ).process(uid, card)
          .attempt
          .flatMap {
            case Right(id) =>
              retries.get.map {
                case Some(w) =>
                  expect.same(id, oid) |+| expect.same(0, w.retriesSoFar)
                case None => failure("Expected one retry")
              }
            case Left(_) => IO.pure(failure("Expected Payment Id"))
          }
      }
    }
  }

  test("cannot create order, run in the background") {
    forall(gen) { case (uid, pid, _, ct, card) =>
      (
        Ref.of[IO, (Int, FiniteDuration)](0 -> 0.seconds),
        Ref.of[IO, Option[GivingUp]](None)
      ).tupled.flatMap { case (acc, retries) =>
        given Background[IO] = TestBackground.counter(acc)
        given Retry[IO]      = TestRetry.givingUp(retries)
        Checkout[IO](
          successfulClient(pid),
          successfulCart(ct),
          failingOrders,
          retryPolicy
        ).process(uid, card)
          .attempt
          .flatMap {
            case Left(OrderError(_)) =>
              (acc.get, retries.get).mapN {
                case (c, Some(g)) =>
                  expect.same(c, 1 -> 1.hour) |+|
                    expect.same(g.totalRetries, MaxRetries)
                case _ =>
                  failure(s"Expected $MaxRetries retries and reschedule")
              }
            case _ =>
              IO.pure(failure("Expected order error"))
          }
      }
    }
  }

  test("failing to delete cart does not affect checkout") {
    forall(gen) { case (uid, pid, oid, ct, card) =>
      Checkout[IO](
        successfulClient(pid),
        failingCart(ct),
        successfulOrders(oid),
        retryPolicy
      ).process(uid, card)
        .map(expect.same(oid, _))
    }
  }

  protected class TestCart extends ShoppingCart[IO] {
    def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = ???
    def get(userId: UserId): IO[CartTotal]                                = ???

    def delete(userId: UserId): IO[Unit] = ???

    def removeItem(userId: UserId, itemId: ItemId): IO[Unit] = ???

    def update(userId: UserId, cart: Cart): IO[Unit] = ???
  }

  protected class TestOrders extends Orders[IO] {
    def get(userId: UserId, orderId: OrderId): IO[Option[Order]] = ???

    def findBy(userId: UserId): IO[List[Order]] = ???

    def create(userId: UserId, paymentId: PaymentId, items: NonEmptyList[CartItem], total: Money): IO[OrderId] = ???
  }

}
