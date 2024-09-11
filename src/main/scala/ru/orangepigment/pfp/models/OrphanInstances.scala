package ru.orangepigment.pfp.models

import cats.syntax.either.*
import io.circe.Decoder.Result
import io.circe.{ Decoder, DecodingFailure, Encoder, HCursor, Json }
import squants.market.{ Money, defaultMoneyContext }

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
          .leftMap(e => DecodingFailure(e.getMessage, Nil))
      }
    }
}
