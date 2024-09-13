package ru.orangepigment.pfp.models

import java.util.UUID

import cats.Show
import cats.derived._
import io.circe.Codec
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec

type CategoryId = CategoryId.Type
object CategoryId extends NewtypeWrapped[UUID] with DerivedCirceCodec {
  given show: Show[CategoryId] = derive
}

type CategoryName = CategoryName.Type
object CategoryName extends NewtypeWrapped[String] with DerivedCirceCodec {
  given show: Show[CategoryName] = derive
}

case class Category(uuid: CategoryId, name: CategoryName) derives Show, Codec.AsObject
