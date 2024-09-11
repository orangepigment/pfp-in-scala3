package ru.orangepigment.pfp.models

import java.util.UUID

import cats.Show
import cats.syntax.contravariant.*
import monix.newtypes.*
import squants.market.Money

type OrderId = OrderId.Type
object OrderId extends NewtypeWrapped[UUID]

type PaymentId = PaymentId.Type
object PaymentId extends NewtypeWrapped[UUID] {
  given show: Show[PaymentId] = {
    Show[UUID].contramap(_.value)
  }
}

case class Order(
    id: OrderId,
    pid: PaymentId,
    items: Map[ItemId, Quantity],
    total: Money
)
