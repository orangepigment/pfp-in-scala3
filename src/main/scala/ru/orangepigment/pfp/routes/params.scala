package ru.orangepigment.pfp.routes

import io.circe.Codec
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*
import io.github.iltotore.iron.constraint.string.*
import io.github.iltotore.iron.circe.given

import java.util.UUID
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec
import ru.orangepigment.pfp.models.{
  BrandId,
  BrandName,
  CategoryId,
  CreateItem,
  ItemDescription,
  ItemId,
  ItemName,
  Password,
  UpdateItem,
  UserName
}
import ru.orangepigment.pfp.util.http4s.NewtypeParamDecoder
import squants.market.USD

object params {

  type UserNameParam = UserNameParam.Type

  object UserNameParam extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceCodec {
    extension (b: UserNameParam.Type) {
      def toDomain: UserName =
        UserName(value.toString().toLowerCase.capitalize)
    }
  }

  type PasswordParam = PasswordParam.Type

  object PasswordParam extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceCodec {
    extension (b: PasswordParam.Type) {
      def toDomain: Password =
        Password(value.toString().toLowerCase.capitalize)
    }
  }

  case class LoginUser(
      username: UserNameParam,
      password: PasswordParam
  ) derives Codec

  case class CreateUser(
      username: UserNameParam,
      password: PasswordParam
  ) derives Codec

  type BrandParam = BrandParam.Type
  object BrandParam extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceCodec with NewtypeParamDecoder {
    extension (b: BrandParam.Type) {
      def toDomain: BrandName =
        BrandName(value.toString().toLowerCase.capitalize)
    }
  }

  type ItemNameParam = ItemNameParam.Type
  object ItemNameParam extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceCodec

  type ItemDescriptionParam = ItemDescriptionParam.Type
  object ItemDescriptionParam extends NewtypeWrapped[String :| Not[Empty]] with DerivedCirceCodec

  // FixMe: valid decimal
  type PriceParam = PriceParam.Type
  object PriceParam extends NewtypeWrapped[String] with DerivedCirceCodec

  case class CreateItemParam(
      name: ItemNameParam,
      description: ItemDescriptionParam,
      price: PriceParam,
      brandId: BrandId,
      categoryId: CategoryId
  ) derives Codec {
    def toDomain: CreateItem =
      CreateItem(
        ItemName(name.value),
        ItemDescription(description.value),
        USD(BigDecimal(price.value)),
        brandId,
        categoryId
      )
  }

  type ItemIdParam = ItemIdParam.Type
  object ItemIdParam extends NewtypeWrapped[String :| ValidUUID] with DerivedCirceCodec

  case class UpdateItemParam(id: ItemIdParam, price: PriceParam) derives Codec {
    def toDomain: UpdateItem =
      UpdateItem(
        ItemId(UUID.fromString(id.value)),
        USD(BigDecimal(price.value))
      )
  }

}
