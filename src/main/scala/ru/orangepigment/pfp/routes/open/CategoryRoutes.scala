package ru.orangepigment.pfp.routes.open

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import ru.orangepigment.pfp.services.Categories

final class CategoryRoutes[F[_]: Monad](
    categories: Categories[F]
) extends Http4sDsl[F] {
  private[open] val prefixPath = "/categories"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(categories.findAll)
  }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
