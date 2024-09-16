package ru.orangepigment.pfp.routes.open

import cats.Monad
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.Empty
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import ru.orangepigment.pfp.routes.params.BrandParam
import ru.orangepigment.pfp.services.Items
import ru.orangepigment.pfp.util.http4s.RefinedParamDecoder

final class ItemRoutes[F[_]: Monad](
    items: Items[F]
) extends Http4sDsl[F] {
  private[open] val prefixPath = "/items"

  // Works but explicit
  object BrandQueryParam
      extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")(using
        BrandParam.newtypeParamDecoder(using
          RefinedParamDecoder.derive[String, Not[Empty]]
        )
      )

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root :? BrandQueryParam(brand) =>
    Ok(
      brand.fold(items.findAll)(b => items.findBy(b.toDomain))
    )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
