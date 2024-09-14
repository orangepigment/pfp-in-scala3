package ru.orangepigment.pfp.routes

import cats.effect.IO
import org.http4s.{ Status => HttpStatus }
import org.http4s.Method.GET
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.scalacheck.Gen
import ru.orangepigment.pfp.models.{ Brand, BrandId, BrandName }
import ru.orangepigment.pfp.routes.open.BrandRoutes
import ru.orangepigment.pfp.services.Brands
import ru.orangepigment.pfp.utils.HttpSuite
import ru.orangepigment.pfp.utils.generators.brandGen

object BrandRoutesSuite extends HttpSuite {

  test("GET brands succeeds") {
    forall(Gen.listOf(brandGen)) { b =>
      val req    = GET(uri"/brands")
      val routes = new BrandRoutes[IO](dataBrands(b)).routes
      expectHttpBodyAndStatus(routes, req)(b, HttpStatus.Ok)
    }
  }

  def dataBrands(brands: List[Brand]): Brands[IO] =
    new TestBrands {
      override def findAll: IO[List[Brand]] = IO.pure(brands)
    }

  protected class TestBrands extends Brands[IO] {
    def findAll: IO[List[Brand]] = ???

    def create(name: BrandName): IO[BrandId] = ???
  }

}
