package ru.orangepigment.pfp.resources

import cats.effect.{ Async, Resource }
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger
import ru.orangepigment.pfp.conf.HttpServerConfig

trait MkHttpServer[F[_]] {
  def newEmber(
      cfg: HttpServerConfig,
      httpApp: HttpApp[F]
  ): Resource[F, Server]
}

object MkHttpServer {
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = implicitly

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  implicit def forAsyncLogger[F[_]: Async: Network: Logger]: MkHttpServer[F] =
    new MkHttpServer[F] {
      def newEmber(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server] =
        EmberServerBuilder
          .default[F]
          .withHost(cfg.host)
          .withPort(cfg.port)
          .withHttpApp(httpApp)
          .build
          .evalTap(showEmberBanner[F])
    }
}
