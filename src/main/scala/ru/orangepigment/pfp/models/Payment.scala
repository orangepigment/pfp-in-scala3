package ru.orangepigment.pfp.models

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.*
import monix.newtypes.*
import ru.orangepigment.pfp.util.NumericLength
import squants.market.Money

type CardHolderName = CardHolderName.Type
object CardHolderName extends NewtypeWrapped[String]

type CardNumber = CardNumber.Type
object CardNumber extends NewtypeWrapped[Long :| NumericLength[16] DescribedAs "Card Number must be a 16-digit number"]

type CardExpiration = CardExpiration.Type
object CardExpiration
    extends NewtypeWrapped[String :| Match["\\d{4}"] DescribedAs "Card Expiration must be a 4-digit number"]

type CVV = CVV.Type
object CVV extends NewtypeWrapped[Int :| NumericLength[3] DescribedAs "Card Number must be a 3-digit number"]

case class Card(
    name: CardHolderName,
    number: CardNumber,
    expiration: CardExpiration,
    cvv: CVV
)

case class Payment(id: UserId, total: Money, card: Card)
