package ru.orangepigment.pfp.routes.admin

import cats.MonadThrow
import cats.syntax.flatMap._
import io.circe.JsonObject
import io.circe.syntax._
import io.github.iltotore.iron.circe.given
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.circe.CirceEntityEncoder._
import ru.orangepigment.pfp.models.AdminUser
import ru.orangepigment.pfp.routes.params.BrandParam
import ru.orangepigment.pfp.services.Brands
import ru.orangepigment.pfp.util.http4s.RefinedRequestDecoder

final class AdminBrandRoutes[
    F[_]: JsonDecoder: MonadThrow
](
    brands: Brands[F]
) extends Http4sDsl[F]
    with RefinedRequestDecoder[F] {
  private[admin] val prefixPath = "/brands"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of { case ar @ POST -> Root as _ =>
      ar.req.decodeR[BrandParam] { bp =>
        brands.create(bp.toDomain).flatMap { id =>
          Created(JsonObject.singleton("brand_id", id.asJson))
        }
      }
    }

  def routes(
      authMiddleware: AuthMiddleware[F, AdminUser]
  ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
