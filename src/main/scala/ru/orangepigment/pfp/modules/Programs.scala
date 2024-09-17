package ru.orangepigment.pfp.modules

import cats.effect.Temporal
import cats.syntax.semigroup.*
import org.typelevel.log4cats.Logger
import retry.RetryPolicies.*
import retry.RetryPolicy
import ru.orangepigment.pfp.conf.CheckoutConfig
import ru.orangepigment.pfp.programs.Checkout
import ru.orangepigment.pfp.util.Background

object Programs {
  def make[F[_]: Background: Logger: Temporal](
      checkoutConfig: CheckoutConfig,
      services: Services[F],
      clients: HttpClients[F]
  ): Programs[F] =
    new Programs[F](checkoutConfig, services, clients) {}
}

sealed trait Programs[F[_]: Background: Logger: Temporal] private (
    cfg: CheckoutConfig,
    services: Services[F],
    clients: HttpClients[F]
) {

  val retryPolicy: RetryPolicy[F] =
    limitRetries[F](cfg.retriesLimit) |+| exponentialBackoff[F](cfg.retriesBackoff)

  val checkout: Checkout[F] = Checkout[F](
    clients.payment,
    services.cart,
    services.orders,
    retryPolicy
  )

}
