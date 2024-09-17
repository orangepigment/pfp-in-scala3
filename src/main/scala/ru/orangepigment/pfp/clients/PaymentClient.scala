package ru.orangepigment.pfp.clients

import cats.effect.MonadCancelThrow
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.either.*
import org.http4s.Method.POST
import org.http4s.{ Status, Uri }
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe.CirceEntityCodec.*
import ru.orangepigment.pfp.conf.PaymentConfig
import ru.orangepigment.pfp.models.Errors.PaymentError
import ru.orangepigment.pfp.models.{ Payment, PaymentId }

trait PaymentClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

object PaymentClient {
  def make[F[_]: MonadCancelThrow: JsonDecoder](
      config: PaymentConfig,
      client: Client[F]
  ): PaymentClient[F] =
    new PaymentClient[F] with Http4sClientDsl[F] {
      def process(payment: Payment): F[PaymentId] =
        Uri
          .fromString(config.value.value + "/payments")
          .liftTo[F]
          .flatMap { uri =>
            client.run(POST(payment, uri)).use { resp =>
              resp.status match {
                case Status.Ok | Status.Conflict =>
                  resp.asJsonDecode[PaymentId]
                case st =>
                  PaymentError(
                    Option(st.reason).getOrElse("unknown")
                  ).raiseError[F, PaymentId]
              }
            }
          }
    }
}
