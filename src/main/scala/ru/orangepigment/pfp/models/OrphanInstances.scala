package ru.orangepigment.pfp.models

import cats.{ Eq, Monoid, Show }
import cats.syntax.contravariant._
import cats.syntax.either._
import dev.profunktor.auth.jwt.JwtToken
import io.circe.Decoder.Result
import io.circe.{ Decoder, DecodingFailure, Encoder, HCursor, Json }
import squants.market.{ Currency, Money, USD, defaultMoneyContext }

object OrphanInstances {

  given Encoder[Money] = new Encoder[Money] {
    final def apply(m: Money): Json = Json.obj(
      ("amount", Json.fromBigDecimal(m.amount)),
      ("currency", Json.fromString(m.currency.code))
    )
  }

  given Decoder[Money] = new Decoder[Money]:
    def apply(c: HCursor): Result[Money] = {
      val rawFields =
        for {
          value    <- c.downField("amount").as[BigDecimal]
          currency <- c.downField("currency").as[String]
        } yield (value, currency)

      rawFields.flatMap { (value, currency) =>
        Money(value, currency)(using defaultMoneyContext).toEither
          .leftMap(e => DecodingFailure(e.getMessage, c.history))
      }
    }

  // Fixme: Must work with all currencies
  given Monoid[Money] with {
    def empty: Money = USD(0)
    def combine(
        x: Money,
        y: Money
    ): Money = x + y
  }

  given currencyEq: Eq[Currency] =
    Eq.and(
      Eq.and(Eq.by(_.code), Eq.by(_.symbol)),
      Eq.by(_.name)
    )

  given moneyEq: Eq[Money] =
    Eq.and(Eq.by(_.amount), Eq.by(_.currency))

  given moneyShow: Show[Money] =
    Show.fromToString

  given tokenEq: Eq[JwtToken] =
    Eq.by(_.value)

  given tokenShow: Show[JwtToken] =
    Show[String].contramap[JwtToken](_.value)

  given tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)
}
