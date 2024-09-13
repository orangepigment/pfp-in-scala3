package ru.orangepigment.pfp.routes.secured

import java.util.UUID

import cats.Monad
import cats.syntax.either._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import ru.orangepigment.pfp.models.{ CommonUser, OrderId }
import ru.orangepigment.pfp.services.Orders

final class OrderRoutes[F[_]: Monad](
    orders: Orders[F]
) extends Http4sDsl[F] {
  import OrderRoutes.*

  private[secured] val prefixPath = "/orders"

  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of {
      case GET -> Root as user =>
        Ok(orders.findBy(user.value.id))
      case GET -> Root / OrderIdVar(orderId) as user =>
        Ok(orders.get(user.value.id, orderId))
    }
  def routes(
      authMiddleware: AuthMiddleware[F, CommonUser]
  ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}

object OrderRoutes {
  private object OrderIdVar {
    def unapply(str: String): Option[OrderId] = {
      Either.catchNonFatal(OrderId(UUID.fromString(str))).toOption
    }
  }
}
