package ru.orangepigment.pfp.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityEncoder.*
import ru.orangepigment.pfp.services.HealthCheck

final class HealthRoutes[F[_]: Monad](
    healthCheck: HealthCheck[F]
) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/healthcheck"
  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root =>
      Ok(healthCheck.status)
    }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
