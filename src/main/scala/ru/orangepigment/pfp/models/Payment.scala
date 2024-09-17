package ru.orangepigment.pfp.models

import cats.Show
import cats.derived.*
import io.circe.{ Codec, Decoder, Encoder }
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*
import io.github.iltotore.iron.constraint.string.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.circe.given
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec
import ru.orangepigment.pfp.models.OrphanInstances.given
import squants.market.Money

type CardHolderName = CardHolderName.Type
object CardHolderName extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[CardHolderName] = derive
}

type CardNumber = CardNumber.Type
object CardNumber
    extends NewtypeWrapped[
      Long :|
        ((Greater[999999999999999L] & Less[10000000000000000L]) DescribedAs "Card Number must be a 16-digit number")
    ]
    with DerivedCirceCodec {
  given show: Show[CardNumber] = derive
}

type CardExpiration = CardExpiration.Type
object CardExpiration extends NewtypeWrapped[String :| Match["\\d{4}"]] with DerivedCirceCodec {
  given show: Show[CardExpiration] = derive
}

type CVV = CVV.Type
object CVV
    extends NewtypeWrapped[Int :| ((Greater[99] & Less[1000]) DescribedAs "CVV must be a 3-digit number")]
    with DerivedCirceCodec {
  given show: Show[CVV] = derive
}

case class Card(
    name: CardHolderName,
    number: CardNumber,
    expiration: CardExpiration,
    cvv: CVV
) derives Show,
      Codec.AsObject

case class Payment(id: UserId, total: Money, card: Card) derives Show, Codec.AsObject
