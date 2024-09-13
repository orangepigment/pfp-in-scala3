package ru.orangepigment.pfp.models

import java.util.UUID

import cats.{ Eq, Show }
import cats.derived._
import io.circe.Codec
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec

type BrandId = BrandId.Type
object BrandId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given show: Show[BrandId] = derive
}

type BrandName = BrandName.Type
object BrandName extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[BrandName] = derive
  given eq: Eq[BrandName]     = derive
}

case class Brand(uuid: BrandId, name: BrandName) derives Show, Codec.AsObject
