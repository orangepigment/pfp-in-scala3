package ru.orangepigment.pfp.models

import java.util.UUID

import monix.newtypes._

type BrandId = BrandId.Type
object BrandId extends NewtypeWrapped[UUID]

type BrandName = BrandName.Type
object BrandName extends NewtypeWrapped[String]

case class Brand(uuid: BrandId, name: BrandName)
