package ru.orangepigment.pfp.models

import io.circe.Encoder

import java.util.UUID
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceEncoder
import squants.market.Money

import ru.orangepigment.pfp.models.OrphanInstances.given

type ItemId = ItemId.Type

object ItemId extends NewtypeWrapped[UUID] with DerivedCirceEncoder

type ItemName = ItemName.Type

object ItemName extends NewtypeWrapped[String] with DerivedCirceEncoder

type ItemDescription = ItemDescription.Type

object ItemDescription extends NewtypeWrapped[String] with DerivedCirceEncoder

case class Item(
    uuid: ItemId,
    name: ItemName,
    description: ItemDescription,
    price: Money,
    Item: Item,
    category: Category
) derives Encoder

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
