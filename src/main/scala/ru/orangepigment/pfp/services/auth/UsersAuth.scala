package ru.orangepigment.pfp.services.auth

import cats.syntax.alternative.*
import cats.syntax.applicative.*
import cats.syntax.eq.*
import cats.syntax.functor.*
import cats.{ Applicative, Functor }
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser.decode
import pdi.jwt.JwtClaim
import ru.orangepigment.pfp.models.OrphanInstances.given
import ru.orangepigment.pfp.models.{ AdminUser, CommonUser, User }

trait UsersAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

object UsersAuth {
  def admin[F[_]: Applicative](
      adminToken: JwtToken,
      adminUser: AdminUser
  ): UsersAuth[F, AdminUser] =
    new UsersAuth[F, AdminUser] {
      def findUser(token: JwtToken)(claim: JwtClaim): F[Option[AdminUser]] =
        (token === adminToken)
          .guard[Option]
          .as(adminUser)
          .pure[F]
    }

  def common[F[_]: Functor](
      redis: RedisCommands[F, String, String]
  ): UsersAuth[F, CommonUser] =
    new UsersAuth[F, CommonUser] {
      def findUser(token: JwtToken)(claim: JwtClaim): F[Option[CommonUser]] =
        redis
          .get(token.value)
          .map {
            _.flatMap { u =>
              decode[User](u).toOption.map(CommonUser.apply)
            }
          }
    }

}
