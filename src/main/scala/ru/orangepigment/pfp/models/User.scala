package ru.orangepigment.pfp.models

import cats.Show
import cats.derived.*
import io.github.iltotore.iron.constraint.string.*

import java.util.UUID
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec

type UserId = UserId.Type
object UserId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given show: Show[UserId] = derive
}

type UserName = UserName.Type
object UserName extends NewtypeWrapped[String] {
  given show: Show[UserName] = derive
}

type Password = Password.Type
object Password extends NewtypeWrapped[String] with DerivedCirceCodec

type EncryptedPassword = EncryptedPassword.Type
object EncryptedPassword extends NewtypeWrapped[String]

case class User(id: UserId, name: UserName) derives Show

case class UserWithPassword(
    id: UserId,
    name: UserName,
    password: EncryptedPassword
)

type CommonUser = CommonUser.Type
object CommonUser extends NewtypeWrapped[User] {
  given show: Show[CommonUser] = derive
}

type AdminUser = AdminUser.Type
object AdminUser extends NewtypeWrapped[User] {
  given show: Show[AdminUser] = derive
}
