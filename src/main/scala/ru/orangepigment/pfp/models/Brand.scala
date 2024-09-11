package ru.orangepigment.pfp.models

import io.circe.Encoder
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*
import io.github.iltotore.iron.constraint.string.*

import java.util.UUID
import ru.orangepigment.pfp.util.http4s.NewtypeParamDecoder
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceEncoder

type BrandId = BrandId.Type

object BrandId extends NewtypeWrapped[UUID] with DerivedCirceEncoder

type BrandName = BrandName.Type

object BrandName extends NewtypeWrapped[String] with DerivedCirceEncoder

case class Brand(uuid: BrandId, name: BrandName) derives Encoder

type BrandParam = BrandParam.Type

object BrandParam extends NewtypeWrapped[String :| Not[Empty]] with NewtypeParamDecoder {
  extension (b: BrandParam.Type) {
    def toDomain: BrandName =
      BrandName(value.toString().toLowerCase.capitalize)
  }
}
