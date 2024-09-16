package ru.orangepigment.pfp.models

import java.util.UUID

import cats.{ Eq, Show }
import cats.derived._
import io.circe.Codec
import io.github.iltotore.iron.constraint.string._
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec

type UserId = UserId.Type
object UserId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given show: Show[UserId] = derive
  given eq: Eq[UserId]     = derive
}

type UserName = UserName.Type
object UserName extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[UserName] = derive
  given eq: Eq[UserName]     = derive
}

type Password = Password.Type
object Password extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[Password] = derive
}

type EncryptedPassword = EncryptedPassword.Type
object EncryptedPassword extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[EncryptedPassword] = derive
  given eq: Eq[EncryptedPassword]     = derive
}

case class User(id: UserId, name: UserName) derives Show, Codec.AsObject

case class UserWithPassword(
    id: UserId,
    name: UserName,
    password: EncryptedPassword
) derives Codec.AsObject

type CommonUser = CommonUser.Type
object CommonUser extends NewtypeWrapped[User] {
  given show: Show[CommonUser] = derive
}

type AdminUser = AdminUser.Type
object AdminUser extends NewtypeWrapped[User] {
  given show: Show[AdminUser] = derive
}
