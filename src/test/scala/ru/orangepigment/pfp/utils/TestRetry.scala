package ru.orangepigment.pfp.utils

import scala.annotation.nowarn

import cats.effect.IO
import cats.effect.kernel.Ref
import retry.RetryDetails.*
import retry.*
import ru.orangepigment.pfp.util.retries.{ Retriable, Retry }

object TestRetry {

  private[utils] def handlerFor[A <: RetryDetails](ref: Ref[IO, Option[A]]): Retry[IO] =
    new Retry[IO] {
      def retry[T](policy: RetryPolicy[IO], retriable: Retriable)(fa: IO[T]): IO[T] = {
        @nowarn
        def onError(e: Throwable, details: RetryDetails): IO[Unit] =
          details match {
            case a: A => ref.set(Some(a))
            case _    => IO.unit
          }

        retryingOnAllErrors[T](policy, onError)(fa)
      }
    }

  def givingUp(ref: Ref[IO, Option[GivingUp]]): Retry[IO] =
    handlerFor[GivingUp](ref)

  def recovering(ref: Ref[IO, Option[WillDelayAndRetry]]): Retry[IO] =
    handlerFor[WillDelayAndRetry](ref)

}
