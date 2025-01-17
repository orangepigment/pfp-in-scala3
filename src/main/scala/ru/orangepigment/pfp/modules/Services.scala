package ru.orangepigment.pfp.modules

import cats.effect.{ Resource, Temporal }
import dev.profunktor.redis4cats.RedisCommands
import ru.orangepigment.pfp.models.ShoppingCartExpiration
import ru.orangepigment.pfp.services.*
import ru.orangepigment.pfp.util.GenUUID
import skunk.Session

object Services {
  def make[F[_]: GenUUID: Temporal](
      redis: RedisCommands[F, String, String],
      postgres: Resource[F, Session[F]],
      cartExpiration: ShoppingCartExpiration
  ): Services[F] = {
    val _items = Items.make[F](postgres)
    new Services[F](
      cart = ShoppingCart.make[F](_items, redis, cartExpiration),
      brands = Brands.make[F](postgres),
      categories = Categories.make[F](postgres),
      items = _items,
      orders = Orders.make[F](postgres),
      healthCheck = HealthCheck.make[F](postgres, redis)
    ) {}
  }
}

sealed trait Services[F[_]] private (
    val cart: ShoppingCart[F],
    val brands: Brands[F],
    val categories: Categories[F],
    val items: Items[F],
    val orders: Orders[F],
    val healthCheck: HealthCheck[F]
)
