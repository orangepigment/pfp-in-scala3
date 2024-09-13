package ru.orangepigment.pfp.services

import scala.concurrent.duration.DurationInt

import cats.effect.{ Resource, Temporal }
import cats.effect.implicits._
import cats.effect.syntax.temporal._
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.functor._
import cats.syntax.parallel._
import dev.profunktor.redis4cats.RedisCommands
import ru.orangepigment.pfp.models.{ AppStatus, PostgresStatus, RedisStatus, Status }
import skunk._
import skunk.codec.all._
import skunk.implicits._

trait HealthCheck[F[_]] {
  def status: F[AppStatus]
}

object HealthCheck {
  def make[F[_]: Temporal](
      postgres: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String]
  ): HealthCheck[F] =
    new HealthCheck[F] {
      val q: Query[Void, Int] =
        sql"SELECT pid FROM pg_stat_activity".query(int4)

      val redisHealth: F[RedisStatus] =
        redis.ping
          .map(_.nonEmpty)
          .timeout(1.second)
          .map(Status._Bool.reverseGet)
          .orElse(Status.Unreachable.pure[F].widen)
          .map(RedisStatus.apply)

      val postgresHealth: F[PostgresStatus] =
        postgres
          .use(_.execute(q))
          .map(_.nonEmpty)
          .timeout(1.second)
          .map(Status._Bool.reverseGet)
          .orElse(Status.Unreachable.pure[F].widen)
          .map(PostgresStatus.apply)

      val status: F[AppStatus] =
        (redisHealth, postgresHealth).parMapN(AppStatus.apply)
    }
}
