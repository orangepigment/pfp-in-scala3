package ru.orangepigment.pfp.models

import io.circe.Encoder
import java.util.UUID
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec

type BrandId = BrandId.Type
object BrandId extends NewtypeWrapped[UUID] with DerivedCirceCodec

type BrandName = BrandName.Type
object BrandName extends NewtypeWrapped[String] with DerivedCirceCodec

case class Brand(uuid: BrandId, name: BrandName) derives Encoder
