package ru.orangepigment.pfp.models

import io.circe.{ Codec, Decoder }
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec
import squants.market.Money
import ru.orangepigment.pfp.models.OrphanInstances.given

type Quantity = Quantity.Type
object Quantity extends NewtypeWrapped[Int] with DerivedCirceCodec

type Cart = Cart.Type
object Cart extends NewtypeWrapped[Map[ItemId, Quantity]] {
  given Decoder[Cart] =
    Decoder
      .decodeMap[ItemId, Quantity]
      .map(Cart.apply)
}

case class CartItem(item: Item, quantity: Quantity) derives Codec
case class CartTotal(items: List[CartItem], total: Money) derives Codec
