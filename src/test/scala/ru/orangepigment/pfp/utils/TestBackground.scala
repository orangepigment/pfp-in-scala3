package ru.orangepigment.pfp.utils

import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.effect.kernel.Ref
import ru.orangepigment.pfp.util.Background

object TestBackground {

  val NoOp: Background[IO] = new Background[IO] {
    def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] = IO.unit
  }

  def counter(ref: Ref[IO, (Int, FiniteDuration)]): Background[IO] =
    new Background[IO] {
      def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] =
        ref.update { case (n, f) => (n + 1, f + duration) }
    }

}
