package ru.orangepigment.pfp.models

import java.util.UUID

import cats.{ Eq, Show }
import cats.derived.*
import cats.syntax.either.*
import io.circe.{ Codec, KeyDecoder, KeyEncoder }
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec
import squants.market.Money
import ru.orangepigment.pfp.models.OrphanInstances.given

type ItemId = ItemId.Type

object ItemId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given keyDecoder: KeyDecoder[ItemId] = new KeyDecoder[ItemId] {
    override def apply(key: String): Option[ItemId] =
      Either.catchNonFatal(ItemId(UUID.fromString(key))).toOption
  }

  given keyEncoder: KeyEncoder[ItemId] = new KeyEncoder[ItemId] {
    def apply(key: ItemId): String = key.value.toString
  }

  given show: Show[ItemId] = derive
}

type ItemName = ItemName.Type
object ItemName extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[ItemName] = derive
  given eq: Eq[ItemName]     = derive
}

type ItemDescription = ItemDescription.Type
object ItemDescription extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[ItemDescription] = derive
}

case class Item(
    uuid: ItemId,
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brand: Brand,
    category: Category
) derives Show,
      Codec.AsObject {
  def cart(q: Quantity): CartItem =
    CartItem(this, q)
}

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
