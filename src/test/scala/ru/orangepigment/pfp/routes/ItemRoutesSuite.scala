package ru.orangepigment.pfp.routes

import cats.effect.IO
import cats.syntax.eq._
import org.http4s.Method.GET
import org.http4s.{Status => HttpStatus}
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.scalacheck.Gen
import ru.orangepigment.pfp.models.{ Brand, BrandName, CreateItem, Item, ItemId, UpdateItem }
import ru.orangepigment.pfp.routes.open.ItemRoutes
import ru.orangepigment.pfp.services.Items
import ru.orangepigment.pfp.utils.HttpSuite
import ru.orangepigment.pfp.utils.generators.{ brandGen, itemGen }

object ItemRoutesSuite extends HttpSuite {

  test("GET items by brand succeeds") {
    val gen = for {
      i <- Gen.listOf(itemGen)
      b <- brandGen
    } yield i -> b

    forall(gen) { case (it, b) =>
      val req = GET(
        uri"/items".withQueryParam("brand", b.name.value)
      )
      val routes   = new ItemRoutes[IO](dataItems(it)).routes
      val expected = it.find(_.brand.name === b.name).toList
      expectHttpBodyAndStatus(routes, req)(expected, HttpStatus.Ok)
    }
  }

  def dataItems(items: List[Item]): Items[IO] = new TestItems {
    override def findAll: IO[List[Item]] =
      IO.pure(items)
    override def findBy(brand: BrandName): IO[List[Item]] =
      IO.pure(items.find(_.brand.name === brand).toList)
  }

  protected class TestItems extends Items[IO] {
    def findAll: IO[List[Item]] = ???

    def findBy(brand: BrandName): IO[List[Item]] = ???

    def findById(itemId: ItemId): IO[Option[Item]] = ???

    def create(item: CreateItem): IO[ItemId] = ???

    def update(item: UpdateItem): IO[Unit] = ???
  }

}
