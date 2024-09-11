package ru.orangepigment.pfp.models

import java.util.UUID

import monix.newtypes._

type UserId = UserId.Type
object UserId extends NewtypeWrapped[UUID]

type UserName = UserName.Type
object UserName extends NewtypeWrapped[String]

type EncryptedPassword = EncryptedPassword.Type
object EncryptedPassword extends NewtypeWrapped[String]

type JwtToken = JwtToken.Type
object JwtToken extends NewtypeWrapped[String]

case class User(id: UserId, name: UserName)

case class UserWithPassword(
    id: UserId,
    name: UserName,
    password: EncryptedPassword
)
