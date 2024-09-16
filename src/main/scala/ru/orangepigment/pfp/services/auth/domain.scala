package ru.orangepigment.pfp.services.auth

import javax.crypto.Cipher
import scala.concurrent.duration.FiniteDuration
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceDecoder

import java.util.UUID

type TokenExpiration = TokenExpiration.Type
object TokenExpiration extends NewtypeWrapped[FiniteDuration]

type EncryptCipher = EncryptCipher.Type
object EncryptCipher extends NewtypeWrapped[Cipher]

type DecryptCipher = DecryptCipher.Type
object DecryptCipher extends NewtypeWrapped[Cipher]

type AdminJwtAuth = AdminJwtAuth.Type
object AdminJwtAuth extends NewtypeWrapped[JwtSymmetricAuth]

type UserJwtAuth = UserJwtAuth.Type
object UserJwtAuth extends NewtypeWrapped[JwtSymmetricAuth]

type ClaimContent = ClaimContent.Type
object ClaimContent extends NewtypeWrapped[UUID] with DerivedCirceDecoder
