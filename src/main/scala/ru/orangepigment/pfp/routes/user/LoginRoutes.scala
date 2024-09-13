package ru.orangepigment.pfp.routes.user

import cats.MonadThrow
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import ru.orangepigment.pfp.models.Errors.{ InvalidPassword, UserNotFound }
import ru.orangepigment.pfp.models.OrphanInstances.given
import ru.orangepigment.pfp.routes.params.LoginUser
import ru.orangepigment.pfp.services.auth.Auth
import ru.orangepigment.pfp.util.http4s.RefinedRequestDecoder

final class LoginRoutes[F[_]: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F]
    with RefinedRequestDecoder[F] {
  private[user] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
    req.decodeR[LoginUser] { user =>
      auth
        .login(user.username.toDomain, user.password.toDomain)
        .flatMap(Ok(_))
        .recoverWith { case UserNotFound(_) | InvalidPassword(_) =>
          Forbidden()
        }
    }
  }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
