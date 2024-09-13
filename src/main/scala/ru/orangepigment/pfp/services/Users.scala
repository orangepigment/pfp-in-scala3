package ru.orangepigment.pfp.services

import cats.effect.{ MonadCancelThrow, Resource }
import cats.syntax.applicativeError.*
import cats.syntax.functor.*
import cats.syntax.flatMap.*
import cats.syntax.option.*
import ru.orangepigment.pfp.models.Errors.UserNameInUse
import ru.orangepigment.pfp.models.{ EncryptedPassword, ID, User, UserId, UserName, UserWithPassword }
import ru.orangepigment.pfp.util.GenUUID
import ru.orangepigment.pfp.util.sqlcodecs.*
import skunk.*
import skunk.implicits.*

trait Users[F[_]] {
  def find(
      username: UserName
  ): F[Option[UserWithPassword]]

  def create(
      username: UserName,
      password: EncryptedPassword
  ): F[UserId]
}

object Users {
  def make[F[_]: GenUUID: MonadCancelThrow](
      postgres: Resource[F, Session[F]]
  ): Users[F] =
    new Users[F] {

      import UserSQL._

      def find(username: UserName): F[Option[UserWithPassword]] =
        postgres.use { session =>
          session.prepare(selectUser).flatMap { q =>
            q.option(username).map {
              case Some(u ~ p) => UserWithPassword(u.id, u.name, p).some
              case _           => none[UserWithPassword]
            }
          }
        }

      def create(username: UserName, password: EncryptedPassword): F[UserId] =
        postgres.use { session =>
          session.prepare(insertUser).flatMap { cmd =>
            ID.make[F, UserId].flatMap { id =>
              cmd
                .execute(User(id, username) -> password)
                .as(id)
                .recoverWith { case SqlState.UniqueViolation(_) =>
                  UserNameInUse(username).raiseError[F, UserId]
                }
            }
          }
        }
    }
}

private object UserSQL {
  val codec: Codec[User ~ EncryptedPassword] =
    (userId ~ userName ~ encryptedPassword).imap { case i ~ n ~ p =>
      User(i, n) -> p
    } { case u ~ p =>
      u.id -> u.name -> p
    }

  val selectUser: Query[UserName, User ~ EncryptedPassword] =
    sql"""
            SELECT * FROM users
            WHERE name = $userName
           """.query(codec)

  val insertUser: Command[User ~ EncryptedPassword] =
    sql"""
          INSERT INTO users
          VALUES ($codec)
          """.command
}
