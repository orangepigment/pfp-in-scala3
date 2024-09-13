package ru.orangepigment.pfp.models

import cats.Show
import io.circe.{ Codec, Decoder }
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec
import squants.market.{ Money, USD }
import ru.orangepigment.pfp.models.OrphanInstances.given

import scala.concurrent.duration.FiniteDuration

type Quantity = Quantity.Type
object Quantity extends NewtypeWrapped[Int] with DerivedCirceCodec {
  given show: Show[Quantity] = derive
}

type Cart = Cart.Type
object Cart extends NewtypeWrapped[Map[ItemId, Quantity]] {
  given Decoder[Cart] =
    Decoder
      .decodeMap[ItemId, Quantity]
      .map(Cart.apply)
}

case class CartItem(item: Item, quantity: Quantity) derives Codec.AsObject {
  def subTotal: Money = USD(item.price.amount * quantity.value)
}

case class CartTotal(items: List[CartItem], total: Money) derives Codec.AsObject

type ShoppingCartExpiration = ShoppingCartExpiration.Type
object ShoppingCartExpiration extends NewtypeWrapped[FiniteDuration]
