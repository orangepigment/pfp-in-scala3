package ru.orangepigment.pfp.services

import ru.orangepigment.pfp.models.{ JwtToken, User, UserName }
import sun.security.util.Password

trait Auth[F[_]] {
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}
