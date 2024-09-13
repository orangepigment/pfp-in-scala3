package ru.orangepigment.pfp.services

import cats.effect.{ Resource, Temporal }
import cats.effect.implicits.*
import cats.effect.syntax.temporal.*
import cats.syntax.applicative.*
import cats.syntax.applicativeError.*
import cats.syntax.functor.*
import cats.syntax.parallel.*
import dev.profunktor.redis4cats.RedisCommands
import ru.orangepigment.pfp.models.{ AppStatus, PostgresStatus, RedisStatus, Status }
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import scala.concurrent.duration.DurationInt

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
