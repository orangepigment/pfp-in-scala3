package ru.orangepigment.pfp.models

import io.circe.Codec

import java.util.UUID
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec

type CategoryId = CategoryId.Type

object CategoryId extends NewtypeWrapped[UUID] with DerivedCirceCodec

type CategoryName = CategoryName.Type

object CategoryName extends NewtypeWrapped[String] with DerivedCirceCodec

case class Category(uuid: CategoryId, name: CategoryName) derives Codec
