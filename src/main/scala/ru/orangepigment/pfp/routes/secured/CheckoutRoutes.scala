package ru.orangepigment.pfp.routes.secured

import cats.MonadThrow
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.show.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import ru.orangepigment.pfp.models.Errors.{ CartNotFound, EmptyCartError, OrderOrPaymentError }
import ru.orangepigment.pfp.models.{ Card, CommonUser }
import ru.orangepigment.pfp.programs.Checkout
import ru.orangepigment.pfp.util.http4s.RefinedRequestDecoder

final class CheckoutRoutes[F[_]: JsonDecoder: MonadThrow](
    checkout: Checkout[F]
) extends Http4sDsl[F]
    with RefinedRequestDecoder[F] {

  private[secured] val prefixPath = "/checkout"
  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of { case ar @ POST -> Root as user =>
      ar.req.decodeR[Card] { card =>
        checkout
          .process(user.value.id, card)
          .flatMap(Created(_))
          .recoverWith {
            case CartNotFound(userId) =>
              NotFound(
                s"Cart not found for user: ${userId.value}"
              )
            case EmptyCartError =>
              BadRequest("Shopping cart is empty!")
            case e: OrderOrPaymentError =>
              BadRequest(e.show)
          }
      }
    }

  def routes(
      authMiddleware: AuthMiddleware[F, CommonUser]
  ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
