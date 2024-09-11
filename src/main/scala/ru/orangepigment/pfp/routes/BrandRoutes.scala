package ru.orangepigment.pfp.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityEncoder.*
import ru.orangepigment.pfp.services.Brands

final class BrandRoutes[F[_]: Monad](
    brands: Brands[F]
) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/brands"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(brands.findAll)
  }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
