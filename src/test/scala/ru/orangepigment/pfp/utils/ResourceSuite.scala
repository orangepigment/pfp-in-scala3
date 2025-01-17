package ru.orangepigment.pfp.utils

import cats.effect.{ IO, Resource }
import cats.syntax.flatMap.*
import weaver.{ Expectations, IOSuite }
import weaver.scalacheck.{ CheckConfig, Checkers }

abstract class ResourceSuite extends IOSuite with Checkers {
  // For it:tests, one test is enough
  override def checkConfig: CheckConfig =
    CheckConfig.default.copy(minimumSuccessful = 1)

  extension (res: Resource[IO, Res]) {
    def beforeAll(f: Res => IO[Unit]): Resource[IO, Res] =
      res.evalTap(f)

    def afterAll(f: Res => IO[Unit]): Resource[IO, Res] =
      res.flatTap(x => Resource.make(IO.unit)(_ => f(x)))
  }

  def testBeforeAfterEach(
      after: Res => IO[Unit],
      before: Res => IO[Unit]
  ): String => (Res => IO[Expectations]) => Unit =
    name =>
      fa =>
        test(name) { res =>
          before(res) >> fa(res).guarantee(after(res))
        }

  def testAfterEach(
      after: Res => IO[Unit]
  ): String => (Res => IO[Expectations]) => Unit =
    testBeforeAfterEach(_ => IO.unit, after)

  def testBeforeEach(
      before: Res => IO[Unit]
  ): String => (Res => IO[Expectations]) => Unit =
    testBeforeAfterEach(before, _ => IO.unit)

}
