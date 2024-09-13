package ru.orangepigment.pfp.routes.admin

import cats.MonadThrow
import cats.syntax.flatMap._
import io.circe.JsonObject
import io.circe.syntax._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.circe.CirceEntityEncoder._
import ru.orangepigment.pfp.models.AdminUser
import ru.orangepigment.pfp.routes.params.{ CreateItemParam, UpdateItemParam }
import ru.orangepigment.pfp.services.Items
import ru.orangepigment.pfp.util.http4s.RefinedRequestDecoder

final class AdminItemRoutes[
    F[_]: JsonDecoder: MonadThrow
](
    items: Items[F]
) extends Http4sDsl[F]
    with RefinedRequestDecoder[F] {
  private[admin] val prefixPath = "/items"
  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      // Create new item
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CreateItemParam] { item =>
          items.create(item.toDomain).flatMap { id =>
            Created(JsonObject.singleton("item_id", id.asJson))
          }
        }

      // Update price of item
      case ar @ PUT -> Root as _ =>
        ar.req.decodeR[UpdateItemParam] { item =>
          items.update(item.toDomain) >> Ok()
        }
    }

  def routes(
      authMiddleware: AuthMiddleware[F, AdminUser]
  ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
