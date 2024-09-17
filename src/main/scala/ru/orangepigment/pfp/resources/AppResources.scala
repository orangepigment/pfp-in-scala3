package ru.orangepigment.pfp.resources

import cats.effect.std.Console
import cats.effect.{ Resource, Temporal }
import cats.syntax.flatMap.*
import cats.syntax.foldable.*
import cats.syntax.parallel.*
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import dev.profunktor.redis4cats.effect.MkRedis
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import ru.orangepigment.pfp.conf.{ AppConfig, PostgreSQLConfig, RedisConfig }
import skunk.{ Session, SessionPool }
import skunk.codec.text.*
import skunk.implicits.*

sealed trait AppResources[F[_]](
    val client: Client[F],
    val postgres: Resource[F, Session[F]],
    val redis: RedisCommands[F, String, String]
)

object AppResources {
  def make[F[_]: Temporal: Console: Logger: MkHttpClient: MkRedis: Network](
      cfg: AppConfig
  ): Resource[F, AppResources[F]] = {
    def checkPostgresConnection(
        postgres: Resource[F, Session[F]]
    ): F[Unit] =
      postgres.use { session =>
        session
          .unique(sql"select version();".query(text))
          .flatMap { v =>
            Logger[F].info(s"Connected to Postgres $v")
          }
      }

    def mkPostgreSqlResource(c: PostgreSQLConfig): SessionPool[F] =
      Session
        .pooled[F](
          host = c.host,
          port = c.port,
          user = c.user,
          password = Some(c.password.value),
          database = c.database,
          max = c.max
        )
        .evalTap(checkPostgresConnection)

    def checkRedisConnection(
        redis: RedisCommands[F, String, String]
    ): F[Unit] =
      redis.info.flatMap {
        _.get("redis_version").traverse_ { v =>
          Logger[F].info(s"Connected to Redis $v")
        }
      }

    def mkRedisResource(c: RedisConfig): Resource[F, RedisCommands[F, String, String]] =
      Redis[F].utf8(c.value.value).evalTap(checkRedisConnection)

    (
      MkHttpClient[F].newEmber(cfg.httpClientConfig),
      mkPostgreSqlResource(cfg.postgreSQL),
      mkRedisResource(cfg.redis)
    ).parMapN(new AppResources[F](_, _, _) {})
  }
}
