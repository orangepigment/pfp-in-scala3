package ru.orangepigment.pfp.routes

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.Method.{ GET, POST }
import org.http4s.implicits.*
import org.http4s.{ Status => HttpStatus }
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.dsl.io.*
import org.http4s.server.AuthMiddleware
import ru.orangepigment.pfp.models.{ Cart, CartTotal, CommonUser, ItemId, Quantity, UserId }
import ru.orangepigment.pfp.routes.secured.CartRoutes
import ru.orangepigment.pfp.services.ShoppingCart
import ru.orangepigment.pfp.utils.HttpSuite
import ru.orangepigment.pfp.utils.generators.{ cartGen, cartTotalGen, commonUserGen }
import squants.market.USD

object CartRoutesSuite extends HttpSuite {

  test("GET shopping cart succeeds") {
    val gen = for {
      u <- commonUserGen
      c <- cartTotalGen
    } yield u -> c

    forall(gen) { case (user, ct) =>
      val req = GET(
        uri"/cart"
      )
      val routes =
        CartRoutes[IO](dataCart(ct))
          .routes(authMiddleware(user))
      expectHttpBodyAndStatus(routes, req)(ct, HttpStatus.Ok)
    }
  }

  test("POST add item to shopping cart succeeds") {
    val gen = for {
      u <- commonUserGen
      c <- cartGen
    } yield u -> c
    forall(gen) { case (user, c) =>
      val req = POST(c, uri"/cart")
      val routes =
        CartRoutes[IO](new TestShoppingCart)
          .routes(authMiddleware(user))
      expectHttpStatus(routes, req)(HttpStatus.Created)
    }
  }

  def authMiddleware(
      authUser: CommonUser
  ): AuthMiddleware[IO, CommonUser] =
    AuthMiddleware(Kleisli.pure(authUser))

  def dataCart(ct: CartTotal): ShoppingCart[IO] = new TestShoppingCart {
    override def get(userId: UserId): IO[CartTotal] = IO.pure(ct)
  }

  protected class TestShoppingCart extends ShoppingCart[IO] {
    def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = IO.unit

    def get(userId: UserId): IO[CartTotal] =
      IO.pure(CartTotal(List.empty, USD(0)))

    def delete(userId: UserId): IO[Unit] = ???

    def removeItem(userId: UserId, itemId: ItemId): IO[Unit] = ???

    def update(userId: UserId, cart: Cart): IO[Unit] = ???
  }

}
