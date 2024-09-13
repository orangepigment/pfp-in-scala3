package ru.orangepigment.pfp.models

import java.util.UUID
import cats.Show
import io.circe.{ Codec, Decoder, Encoder }
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec
import squants.market.Money
import ru.orangepigment.pfp.models.OrphanInstances.given

type OrderId = OrderId.Type
object OrderId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given show: Show[OrderId] = derive
}

type PaymentId = PaymentId.Type
object PaymentId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given show: Show[PaymentId] = derive
}

case class Order(
    id: OrderId,
    paymentId: PaymentId,
    items: Map[ItemId, Quantity],
    total: Money
) derives Codec.AsObject

object Order {
  given Decoder[Map[ItemId, Quantity]] = Decoder.decodeMap[ItemId, Quantity]
  given Encoder[Map[ItemId, Quantity]] = Encoder.encodeMap[ItemId, Quantity]
}
