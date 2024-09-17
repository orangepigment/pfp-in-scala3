package ru.orangepigment.pfp.clients

import cats.effect.IO
import io.github.iltotore.iron.*
import org.http4s.Method.POST
import org.http4s.client.Client
import org.http4s.{ HttpRoutes, Response }
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import ru.orangepigment.pfp.conf.{ PaymentConfig, PaymentURI }
import ru.orangepigment.pfp.models.Errors.PaymentError
import ru.orangepigment.pfp.models.PaymentId
import ru.orangepigment.pfp.utils.generators.{ paymentGen, paymentIdGen }
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object PaymentClientSuite extends SimpleIOSuite with Checkers {

  val config = PaymentConfig(PaymentURI("http://localhost".refineUnsafe))

  def routes(mkResponse: IO[Response[IO]]) =
    HttpRoutes
      .of[IO] { case POST -> Root / "payments" =>
        mkResponse
      }
      .orNotFound

  val gen = for {
    i <- paymentIdGen
    p <- paymentGen
  } yield i -> p

  test("Response Ok (200)") {
    forall(gen) { case (pid, payment) =>
      val client = Client.fromHttpApp(routes(Ok(pid)))
      PaymentClient
        .make[IO](config, client)
        .process(payment)
        .map(expect.same(pid, _))
    }
  }

  test("Internal Server Error response (500)") {
    forall(paymentGen) { payment =>
      val client = Client.fromHttpApp(routes(InternalServerError()))
      PaymentClient
        .make[IO](config, client)
        .process(payment)
        .attempt
        .map {
          case Left(e) =>
            expect.same(PaymentError("Internal Server Error"), e)
          case Right(_) =>
            failure("expected payment error")
        }
    }
  }

}
