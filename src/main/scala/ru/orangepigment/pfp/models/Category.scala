package ru.orangepigment.pfp.models

import java.util.UUID

import monix.newtypes._

type CategoryId = CategoryId.Type

object CategoryId extends NewtypeWrapped[UUID]

type CategoryName = CategoryName.Type

object CategoryName extends NewtypeWrapped[String]

case class Category(uuid: CategoryId, name: CategoryName)
