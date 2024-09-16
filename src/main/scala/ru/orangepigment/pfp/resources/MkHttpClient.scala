package ru.orangepigment.pfp.resources

import cats.effect.{ Async, Resource }
import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import ru.orangepigment.pfp.conf.HttpClientConfig

trait MkHttpClient[F[_]] {
  def newEmber(c: HttpClientConfig): Resource[F, Client[F]]
}

object MkHttpClient {
  def apply[F[_]: MkHttpClient]: MkHttpClient[F] = summon
  given forAsync[F[_]: Async: Network]: MkHttpClient[F] =
    new MkHttpClient[F] {
      def newEmber(c: HttpClientConfig): Resource[F, Client[F]] =
        EmberClientBuilder
          .default[F]
          .withTimeout(c.timeout)
          .withIdleTimeInPool(c.idleTimeInPool)
          .build
    }
}
