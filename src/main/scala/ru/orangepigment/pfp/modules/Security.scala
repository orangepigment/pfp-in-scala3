package ru.orangepigment.pfp.modules

import cats.ApplicativeThrow
import cats.effect.{ Resource, Sync }
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import dev.profunktor.auth.jwt.*
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser.decode as jsonDecode
import pdi.jwt.JwtAlgorithm
import ru.orangepigment.pfp.conf.AppConfig
import ru.orangepigment.pfp.models.{ AdminUser, CommonUser, User, UserId, UserName }
import ru.orangepigment.pfp.services.Users
import ru.orangepigment.pfp.services.auth.{
  AdminJwtAuth,
  Auth,
  ClaimContent,
  Crypto,
  JwtExpire,
  Tokens,
  UserJwtAuth,
  UsersAuth
}
import skunk.Session

object Security {
  def make[F[_]: Sync](
      cfg: AppConfig,
      postgres: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String]
  ): F[Security[F]] = {

    val adminJwtAuth: AdminJwtAuth =
      AdminJwtAuth(
        JwtAuth
          .hmac(
            cfg.adminJwtConfig.secretKey.value.value,
            JwtAlgorithm.HS256
          )
      )

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConfig.value.value,
            JwtAlgorithm.HS256
          )
      )

    val adminToken = JwtToken(cfg.adminJwtConfig.adminToken.value.value)

    for {
      adminClaim <- jwtDecode[F](adminToken, adminJwtAuth.value)
      content    <- ApplicativeThrow[F].fromEither(jsonDecode[ClaimContent](adminClaim.content))
      adminUser = AdminUser(User(UserId(content.value), UserName("admin")))
      tokens <- JwtExpire.make[F].map(Tokens.make[F](_, cfg.tokenConfig.value, cfg.tokenExpiration))
      crypto <- Crypto.make[F](cfg.passwordSalt.value)
      users     = Users.make[F](postgres)
      auth      = Auth.make[F](cfg.tokenExpiration, tokens, users, redis, crypto)
      adminAuth = UsersAuth.admin[F](adminToken, adminUser)
      usersAuth = UsersAuth.common[F](redis)
    } yield new Security[F](auth, adminAuth, usersAuth, adminJwtAuth, userJwtAuth) {}

  }
}

sealed trait Security[F[_]] private (
    val auth: Auth[F],
    val adminAuth: UsersAuth[F, AdminUser],
    val usersAuth: UsersAuth[F, CommonUser],
    val adminJwtAuth: AdminJwtAuth,
    val userJwtAuth: UserJwtAuth
)
