package ru.orangepigment.pfp.conf

import scala.concurrent.duration.DurationInt

import cats.effect.Async
import cats.syntax.parallel.*
import ciris.*
import com.comcast.ip4s.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*
import io.github.iltotore.iron.cats.given
import ru.orangepigment.pfp.conf.AppEnvironment.{ Prod, Test }
import ru.orangepigment.pfp.models.ShoppingCartExpiration
import ru.orangepigment.pfp.services.auth.TokenExpiration
import ru.orangepigment.pfp.conf.decoders.given

object AppConfig {

  // Ciris promotes configuration as code
  def load[F[_]: Async]: F[AppConfig] =
    env("SC_APP_ENV")
      .as[AppEnvironment]
      .flatMap {
        case Test =>
          default[F](
            RedisURI("redis://localhost"),
            PaymentURI("https://payments.free.beeceptor.com")
          )
        case Prod =>
          default[F](
            RedisURI("redis://10.123.154.176"),
            PaymentURI("https://payments.net/api")
          )
      }
      .load[F]

  private def default[F[_]](
      redisUri: RedisURI,
      paymentUri: PaymentURI
  ): ConfigValue[F, AppConfig] =
    (
      env("SC_JWT_SECRET_KEY").as[JwtSecretKeyConfig].secret,
      env("SC_JWT_CLAIM").as[JwtClaimConfig].secret,
      env("SC_ACCESS_TOKEN_SECRET_KEY").as[JwtAccessTokenKeyConfig].secret,
      env("SC_ADMIN_USER_TOKEN").as[AdminUserTokenConfig].secret,
      env("SC_PASSWORD_SALT").as[PasswordSalt].secret,
      env("SC_POSTGRES_PASSWORD").as[String :| Not[Empty]].secret
    ).parMapN { (jwtSecretKey, jwtClaim, tokenKey, adminToken, salt, postgresPassword) =>
      AppConfig(
        AdminJwtConfig(jwtSecretKey, jwtClaim, adminToken),
        tokenKey,
        salt,
        TokenExpiration(30.minutes),
        ShoppingCartExpiration(30.minutes),
        CheckoutConfig(
          retriesLimit = 3,
          retriesBackoff = 10.milliseconds
        ),
        PaymentConfig(paymentUri),
        HttpClientConfig(
          timeout = 60.seconds,
          idleTimeInPool = 30.seconds
        ),
        PostgreSQLConfig(
          host = "localhost",
          port = 5432,
          user = "postgres",
          password = postgresPassword,
          database = "store",
          max = 10
        ),
        RedisConfig(redisUri),
        HttpServerConfig(
          host = host"0.0.0.0",
          port = port"8080"
        )
      )
    }

}

case class AppConfig(
    adminJwtConfig: AdminJwtConfig,
    tokenConfig: Secret[JwtAccessTokenKeyConfig],
    passwordSalt: Secret[PasswordSalt],
    tokenExpiration: TokenExpiration,
    cartExpiration: ShoppingCartExpiration,
    checkoutConfig: CheckoutConfig,
    paymentConfig: PaymentConfig,
    httpClientConfig: HttpClientConfig,
    postgreSQL: PostgreSQLConfig,
    redis: RedisConfig,
    httpServerConfig: HttpServerConfig
)
