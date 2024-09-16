package ru.orangepigment.pfp.modules

import cats.effect.Async
import cats.syntax.semigroupk.*
import dev.profunktor.auth.JwtAuthMiddleware
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.*
import ru.orangepigment.pfp.models.{ AdminUser, CommonUser }
import ru.orangepigment.pfp.routes.{ HealthRoutes, version }
import ru.orangepigment.pfp.routes.admin.*
import ru.orangepigment.pfp.routes.open.*
import ru.orangepigment.pfp.routes.secured.*
import ru.orangepigment.pfp.routes.user.*

import scala.annotation.nowarn
import scala.concurrent.duration.DurationInt

object HttpApi {
  def make[F[_]: Async](
      services: Services[F],
      programs: Programs[F],
      security: Security[F]
  ): HttpApi[F] =
    new HttpApi[F](services, programs, security) {}
}

sealed abstract class HttpApi[F[_]: Async] private (
    services: Services[F],
    programs: Programs[F],
    security: Security[F]
) {
  private val adminMiddleware =
    JwtAuthMiddleware[F, AdminUser](security.adminJwtAuth.value, security.adminAuth.findUser)
  private val usersMiddleware =
    JwtAuthMiddleware[F, CommonUser](security.userJwtAuth.value, security.usersAuth.findUser)

  // Auth routes
  private val loginRoutes  = LoginRoutes[F](security.auth).routes
  private val logoutRoutes = LogoutRoutes[F](security.auth).routes(usersMiddleware)
  private val userRoutes   = UserRoutes[F](security.auth).routes

  // Open routes
  private val healthRoutes   = HealthRoutes[F](services.healthCheck).routes
  private val brandRoutes    = BrandRoutes[F](services.brands).routes
  private val categoryRoutes = CategoryRoutes[F](services.categories).routes
  private val itemRoutes     = ItemRoutes[F](services.items).routes

  // Secured routes
  private val cartRoutes     = CartRoutes[F](services.cart).routes(usersMiddleware)
  private val checkoutRoutes = CheckoutRoutes[F](programs.checkout).routes(usersMiddleware)
  private val orderRoutes    = OrderRoutes[F](services.orders).routes(usersMiddleware)

  // Admin routes
  private val adminBrandRoutes    = AdminBrandRoutes[F](services.brands).routes(adminMiddleware)
  private val adminCategoryRoutes = AdminCategoryRoutes[F](services.categories).routes(adminMiddleware)
  private val adminItemRoutes     = AdminItemRoutes[F](services.items).routes(adminMiddleware)

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] =
    healthRoutes <+> itemRoutes <+> brandRoutes <+>
      categoryRoutes <+> loginRoutes <+> userRoutes <+>
      logoutRoutes <+> cartRoutes <+> orderRoutes <+>
      checkoutRoutes

  private val adminRoutes: HttpRoutes[F] =
    adminItemRoutes <+> adminBrandRoutes <+> adminCategoryRoutes

  private val routes: HttpRoutes[F] = Router(
    version.v1            -> openRoutes,
    version.v1 + "/admin" -> adminRoutes
  )

  @nowarn
  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { (http: HttpRoutes[F]) =>
      AutoSlash(http)
    } andThen { (http: HttpRoutes[F]) =>
      CORS(http) // FixMe: vulnerability https://github.com/http4s/http4s/security/advisories/GHSA-52cf-226f-rhr6
    } andThen { (http: HttpRoutes[F]) =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { (http: HttpApp[F]) =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { (http: HttpApp[F]) =>
      ResponseLogger.httpApp(true, true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
