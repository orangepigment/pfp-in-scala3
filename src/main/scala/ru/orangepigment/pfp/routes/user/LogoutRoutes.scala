package ru.orangepigment.pfp.routes.user

import cats.Monad
import cats.syntax.apply._
import cats.syntax.foldable._
import dev.profunktor.auth.AuthHeaders
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import ru.orangepigment.pfp.models.CommonUser
import ru.orangepigment.pfp.services.auth.Auth

final class LogoutRoutes[F[_]: Monad](
    auth: Auth[F]
) extends Http4sDsl[F] {
  private[user] val prefixPath = "/auth"
  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of { case ar @ POST -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken(ar.req)
        .traverse_(auth.logout(_, user.value.name)) *>
        NoContent()
    }

  def routes(
      authMiddleware: AuthMiddleware[F, CommonUser]
  ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
