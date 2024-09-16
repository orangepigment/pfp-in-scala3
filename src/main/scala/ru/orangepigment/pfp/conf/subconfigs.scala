package ru.orangepigment.pfp.conf

import scala.concurrent.duration.FiniteDuration

import cats.Show
import ciris.{ ConfigDecoder, Secret }
import ciris.circe.*
import com.comcast.ip4s.{ Host, Port }
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*
import io.github.iltotore.iron.constraint.numeric.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.circe.given
import monix.newtypes.NewtypeWrapped
import monix.newtypes.integrations.DerivedCirceDecoder

type PaymentURI = PaymentURI.Type
object PaymentURI extends NewtypeWrapped[String :| Not[Empty]]

type PaymentConfig = PaymentConfig.Type
object PaymentConfig extends NewtypeWrapped[PaymentURI]

type AdminUserTokenConfig = AdminUserTokenConfig.Type
object AdminUserTokenConfig extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceDecoder {
  given Show[AdminUserTokenConfig] = derive
  given ConfigDecoder[String, AdminUserTokenConfig] =
    circeConfigDecoder("AdminUserTokenConfig")
}

type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.Type
object JwtAccessTokenKeyConfig extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceDecoder {
  given Show[JwtAccessTokenKeyConfig] = derive
  given ConfigDecoder[String, JwtAccessTokenKeyConfig] =
    circeConfigDecoder("JwtAccessTokenKeyConfig")
}

type JwtSecretKeyConfig = JwtSecretKeyConfig.Type
object JwtSecretKeyConfig extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceDecoder {
  given Show[JwtSecretKeyConfig] = derive
  given ConfigDecoder[String, JwtSecretKeyConfig] =
    circeConfigDecoder("JwtSecretKeyConfig")
}

type JwtClaimConfig = JwtClaimConfig.Type
object JwtClaimConfig extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceDecoder {
  given Show[JwtClaimConfig] = derive
  given ConfigDecoder[String, JwtClaimConfig] =
    circeConfigDecoder("JwtClaimConfig")
}

case class AdminJwtConfig(
    secretKey: Secret[JwtSecretKeyConfig],
    claimStr: Secret[JwtClaimConfig],
    adminToken: Secret[AdminUserTokenConfig]
)

type PasswordSalt = PasswordSalt.Type

object PasswordSalt extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceDecoder {
  given show: Show[PasswordSalt] = derive
  given ConfigDecoder[String, PasswordSalt] =
    circeConfigDecoder("PasswordSalt")
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
