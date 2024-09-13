package ru.orangepigment.pfp.models

import cats.Show
import cats.derived.*
import io.circe.Codec

import java.util.UUID
import monix.newtypes.*
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
