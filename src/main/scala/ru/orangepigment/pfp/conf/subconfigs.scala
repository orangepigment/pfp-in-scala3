package ru.orangepigment.pfp.conf

import scala.concurrent.duration.FiniteDuration

import cats.Show
import cats.syntax.either.*
import ciris.{ ConfigDecoder, Secret }
import com.comcast.ip4s.{ Host, Port }
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*
import io.github.iltotore.iron.constraint.numeric.*
import io.github.iltotore.iron.cats.given
import monix.newtypes.NewtypeWrapped

type PaymentURI = PaymentURI.Type
object PaymentURI extends NewtypeWrapped[String :| Not[Empty]]

type PaymentConfig = PaymentConfig.Type
object PaymentConfig extends NewtypeWrapped[PaymentURI]

type AdminUserTokenConfig = AdminUserTokenConfig.Type
object AdminUserTokenConfig extends NewtypeWrapped[String :| Not[Empty]] {
  given Show[AdminUserTokenConfig] = derive
  given ConfigDecoder[String, AdminUserTokenConfig] =
    ConfigDecoder[String].mapOption("ru.orangepigment.pfp.conf.AdminUserTokenConfig")(s =>
      Either.catchNonFatal(AdminUserTokenConfig(s.refineUnsafe)).toOption
    )
}

type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.Type
object JwtAccessTokenKeyConfig extends NewtypeWrapped[String :| Not[Empty]] {
  given Show[JwtAccessTokenKeyConfig] = derive
  given ConfigDecoder[String, JwtAccessTokenKeyConfig] =
    ConfigDecoder[String].mapOption("ru.orangepigment.pfp.conf.JwtAccessTokenKeyConfig")(s =>
      Either.catchNonFatal(JwtAccessTokenKeyConfig(s.refineUnsafe)).toOption
    )
}

type JwtSecretKeyConfig = JwtSecretKeyConfig.Type
object JwtSecretKeyConfig extends NewtypeWrapped[String :| Not[Empty]] {
  given Show[JwtSecretKeyConfig] = derive
  given ConfigDecoder[String, JwtSecretKeyConfig] =
    ConfigDecoder[String].mapOption("ru.orangepigment.pfp.conf.JwtSecretKeyConfig")(s =>
      Either.catchNonFatal(JwtSecretKeyConfig(s.refineUnsafe)).toOption
    )
}

type JwtClaimConfig = JwtClaimConfig.Type
object JwtClaimConfig extends NewtypeWrapped[String :| Not[Empty]] {
  given Show[JwtClaimConfig] = derive
  given ConfigDecoder[String, JwtClaimConfig] =
    ConfigDecoder[String].mapOption("ru.orangepigment.pfp.conf.JwtClaimConfig")(s =>
      Either.catchNonFatal(JwtClaimConfig(s.refineUnsafe)).toOption
    )
}

case class AdminJwtConfig(
    secretKey: Secret[JwtSecretKeyConfig],
    claimStr: Secret[JwtClaimConfig],
    adminToken: Secret[AdminUserTokenConfig]
)

type PasswordSalt = PasswordSalt.Type

object PasswordSalt extends NewtypeWrapped[String :| Not[Empty]] {
  given show: Show[PasswordSalt] = derive
  given ConfigDecoder[String, PasswordSalt] =
    ConfigDecoder[String].mapOption("ru.orangepigment.pfp.conf.AppEnvironment")(s =>
      Either.catchNonFatal(PasswordSalt(s.refineUnsafe)).toOption
    )
}

case class CheckoutConfig(
    retriesLimit: Int :| Greater[0],
    retriesBackoff: FiniteDuration
)

case class PostgreSQLConfig(
    host: String :| Not[Empty],
    port: Int :| Greater[0],
    user: String :| Not[Empty],
    password: Secret[String :| Not[Empty]],
    database: String :| Not[Empty],
    max: Int :| Greater[0]
)

type RedisURI = RedisURI.Type
object RedisURI extends NewtypeWrapped[String :| Not[Empty]]

type RedisConfig = RedisConfig.Type
object RedisConfig extends NewtypeWrapped[RedisURI]

case class HttpServerConfig(
    host: Host,
    port: Port
)

case class HttpClientConfig(
    timeout: FiniteDuration,
    idleTimeInPool: FiniteDuration
)
