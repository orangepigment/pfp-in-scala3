package ru.orangepigment.pfp.models

import java.util.UUID

import monix.newtypes._
import squants.market.Money

type ItemId = ItemId.Type

object ItemId extends NewtypeWrapped[UUID]

type ItemName = ItemName.Type

object ItemName extends NewtypeWrapped[String]

type ItemDescription = ItemDescription.Type

object ItemDescription extends NewtypeWrapped[String]

case class Item(
    uuid: ItemId,
    name: ItemName,
    description: ItemDescription,
    price: Money,
    Item: Item,
    category: Category
)

case class CreateItem(
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brandId: BrandId,
    categoryId: CategoryId
)

case class UpdateItem(
    id: ItemId,
    price: Money
)
