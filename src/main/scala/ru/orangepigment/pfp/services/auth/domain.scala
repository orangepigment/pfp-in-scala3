package ru.orangepigment.pfp.services.auth

import javax.crypto.Cipher

import scala.concurrent.duration.FiniteDuration

import cats.Show
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*
import io.github.iltotore.iron.cats.given
import monix.newtypes.*

type TokenExpiration = TokenExpiration.Type
object TokenExpiration extends NewtypeWrapped[FiniteDuration]

type EncryptCipher = EncryptCipher.Type
object EncryptCipher extends NewtypeWrapped[Cipher]

type DecryptCipher = DecryptCipher.Type
object DecryptCipher extends NewtypeWrapped[Cipher]

type PasswordSalt = PasswordSalt.Type
object PasswordSalt extends NewtypeWrapped[String :| Not[Empty]] {
  given show: Show[PasswordSalt] = derive
}

type AdminJwtAuth = AdminJwtAuth.Type
object AdminJwtAuth extends NewtypeWrapped[JwtSymmetricAuth]

type UserJwtAuth = UserJwtAuth.Type
object UserJwtAuth extends NewtypeWrapped[JwtSymmetricAuth]
