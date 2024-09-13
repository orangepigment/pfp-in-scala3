package ru.orangepigment.pfp.models

import cats.Show
import cats.derived.*
import io.circe.Codec
import java.util.UUID
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec

type BrandId = BrandId.Type
object BrandId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given show: Show[BrandId] = derive
}

type BrandName = BrandName.Type
object BrandName extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[BrandName] = derive
}

case class Brand(uuid: BrandId, name: BrandName) derives Show, Codec.AsObject
