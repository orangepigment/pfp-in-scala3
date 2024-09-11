package ru.orangepigment.pfp.util

import scala.concurrent.duration.FiniteDuration

import cats.syntax.apply._
import cats.syntax.functor._
import cats.effect.Temporal
import cats.effect.std.Supervisor

trait Background[F[_]] {
  def schedule[A](
      fa: F[A],
      duration: FiniteDuration
  ): F[Unit]
}

object Background {
  def apply[F[_]: Background]: Background[F] = summon

  given bgInstance[F[_]](using
      S: Supervisor[F],
      T: Temporal[F]
  ): Background[F] =
    new Background[F] {
      def schedule[A](
          fa: F[A],
          duration: FiniteDuration
      ): F[Unit] =
        S.supervise(T.sleep(duration) *> fa).void
    }
}
