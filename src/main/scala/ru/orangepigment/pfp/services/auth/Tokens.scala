package ru.orangepigment.pfp.services.auth

import ru.orangepigment.pfp.conf._
import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import dev.profunktor.auth.jwt._
import io.circe.syntax._
import pdi.jwt._
import ru.orangepigment.pfp.util.GenUUID

trait Tokens[F[_]] {
  def create: F[JwtToken]
}

object Tokens {
  def make[F[_]: GenUUID: Monad](
      jwtExpire: JwtExpire[F],
      config: JwtSecretKeyConfig,
      exp: TokenExpiration
  ): Tokens[F] =
    new Tokens[F] {
      def create: F[JwtToken] =
        for {
          uuid  <- GenUUID[F].make
          claim <- jwtExpire.expiresIn(JwtClaim(uuid.asJson.noSpaces), exp)
          secretKey = JwtSecretKey(config.value)
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        } yield token
    }
}
