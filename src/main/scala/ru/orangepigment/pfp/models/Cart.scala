package ru.orangepigment.pfp.models

import scala.concurrent.duration.FiniteDuration

import cats.Show
import cats.derived._
import io.circe.{ Codec, Decoder, Encoder }
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec
import squants.market.{ Money, USD }
import ru.orangepigment.pfp.models.OrphanInstances.given

type Quantity = Quantity.Type
object Quantity extends NewtypeWrapped[Int] with DerivedCirceCodec {
  given show: Show[Quantity] = derive
}

type Cart = Cart.Type
object Cart extends NewtypeWrapped[Map[ItemId, Quantity]] {
  given show: Show[Cart] = derive

  given Decoder[Cart] =
    Decoder
      .decodeMap[ItemId, Quantity]
      .map(Cart.apply)

  given Encoder[Cart] =
    Encoder
      .encodeMap[ItemId, Quantity]
      .contramap(_.value)
}

case class CartItem(item: Item, quantity: Quantity) derives Show, Codec.AsObject {
  def subTotal: Money = USD(item.price.amount * quantity.value)
}

case class CartTotal(items: List[CartItem], total: Money) derives Show, Codec.AsObject

type ShoppingCartExpiration = ShoppingCartExpiration.Type
object ShoppingCartExpiration extends NewtypeWrapped[FiniteDuration]
