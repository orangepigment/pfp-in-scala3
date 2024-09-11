package ru.orangepigment.pfp.models

import io.circe.Encoder

import java.util.UUID
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceEncoder

type CategoryId = CategoryId.Type

object CategoryId extends NewtypeWrapped[UUID] with DerivedCirceEncoder

type CategoryName = CategoryName.Type

object CategoryName extends NewtypeWrapped[String] with DerivedCirceEncoder

case class Category(uuid: CategoryId, name: CategoryName) derives Encoder
