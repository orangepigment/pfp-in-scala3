package ru.orangepigment.pfp.routes.secured

import java.util.UUID

import cats.Monad
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import ru.orangepigment.pfp.models.{ Cart, CommonUser, ItemId }
import ru.orangepigment.pfp.services.ShoppingCart

final class CartRoutes[F[_]: JsonDecoder: Monad](
    shoppingCart: ShoppingCart[F]
) extends Http4sDsl[F] {
  import CartRoutes.*

  private[secured] val prefixPath = "/cart"

  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of {
      // Get shopping cart
      case GET -> Root as user =>
        Ok(shoppingCart.get(user.value.id))
      // Add items to the cart
      case ar @ POST -> Root as user =>
        ar.req.asJsonDecode[Cart].flatMap {
          _.value
            .map { case (id, quantity) =>
              shoppingCart.add(user.value.id, id, quantity)
            }
            .toList
            .sequence *> Created()
        }
      // Modify items in the cart
      case ar @ PUT -> Root as user =>
        ar.req.asJsonDecode[Cart].flatMap { cart =>
          shoppingCart.update(user.value.id, cart) *> Ok()
        }
      // Remove item from the cart
      case DELETE -> Root / ItemIdVar(itemId) as user =>
        shoppingCart.removeItem(user.value.id, itemId) *>
          NoContent()
    }
  def routes(
      authMiddleware: AuthMiddleware[F, CommonUser]
  ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}

object CartRoutes {
  private object ItemIdVar {
    def unapply(str: String): Option[ItemId] =
      Either.catchNonFatal(ItemId(UUID.fromString(str))).toOption
  }
}
