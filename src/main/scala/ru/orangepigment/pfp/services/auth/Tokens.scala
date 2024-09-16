package ru.orangepigment.pfp.services.auth

import ru.orangepigment.pfp.conf.*
import cats.Monad
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import dev.profunktor.auth.jwt.*
import io.circe.syntax.*
import pdi.jwt.*
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
