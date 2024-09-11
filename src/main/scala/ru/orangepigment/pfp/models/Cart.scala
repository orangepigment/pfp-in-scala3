package ru.orangepigment.pfp.models

import monix.newtypes._
import squants.market.Money

type Quantity = Quantity.Type
object Quantity extends NewtypeWrapped[Int]

type Cart = Cart.Type
object Cart extends NewtypeWrapped[Map[ItemId, Quantity]]

case class CartItem(item: Item, quantity: Quantity)
case class CartTotal(items: List[CartItem], total: Money)
