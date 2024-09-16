package ru.orangepigment.pfp.routes.admin

import cats.MonadThrow
import cats.syntax.flatMap.*
import io.circe.JsonObject
import io.circe.syntax.*
import io.github.iltotore.iron.circe.given
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import ru.orangepigment.pfp.models.AdminUser
import ru.orangepigment.pfp.routes.params.CategoryParam
import ru.orangepigment.pfp.services.Categories
import ru.orangepigment.pfp.util.http4s.RefinedRequestDecoder

final class AdminCategoryRoutes[
    F[_]: JsonDecoder: MonadThrow
](
    categories: Categories[F]
) extends Http4sDsl[F]
    with RefinedRequestDecoder[F] {
  private[admin] val prefixPath = "/brands"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of { case ar @ POST -> Root as _ =>
      ar.req.decodeR[CategoryParam] { c =>
        categories.create(c.toDomain).flatMap { id =>
          Created(JsonObject.singleton("category_id", id.asJson))
        }
      }
    }

  def routes(
      authMiddleware: AuthMiddleware[F, AdminUser]
  ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
