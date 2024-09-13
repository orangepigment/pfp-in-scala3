package ru.orangepigment.pfp.routes.user

import cats.MonadThrow
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.show.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import ru.orangepigment.pfp.models.Errors.UserNameInUse
import ru.orangepigment.pfp.models.OrphanInstances.given
import ru.orangepigment.pfp.routes.params.CreateUser
import ru.orangepigment.pfp.services.auth.Auth
import ru.orangepigment.pfp.util.http4s.RefinedRequestDecoder

final class UserRoutes[F[_]: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F]
    with RefinedRequestDecoder[F] {
  private[user] val prefixPath = "/auth"
  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root / "users" =>
      req
        .decodeR[CreateUser] { user =>
          auth
            .newUser(
              user.username.toDomain,
              user.password.toDomain
            )
            .flatMap(Created(_))
            .recoverWith { case UserNameInUse(u) =>
              Conflict(u.show)
            }
        }
    }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
