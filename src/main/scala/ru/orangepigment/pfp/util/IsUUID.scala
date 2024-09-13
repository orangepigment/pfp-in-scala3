package ru.orangepigment.pfp.util

import java.util.UUID
import monocle.Iso
import ru.orangepigment.pfp.models.{ BrandId, CategoryId, ItemId, OrderId, UserId }

trait IsUUID[A] {
  def _UUID: Iso[UUID, A]
}

object IsUUID {
  def apply[A: IsUUID]: IsUUID[A] = summon

  given identityUUID: IsUUID[UUID] = new IsUUID[UUID] {
    val _UUID: Iso[UUID, UUID] = Iso[UUID, UUID](identity)(identity)
  }

  given brandIdUUID: IsUUID[BrandId] = new IsUUID[BrandId] {
    val _UUID: Iso[UUID, BrandId] = Iso[UUID, BrandId](BrandId.apply)(_.value)
  }

  given categoryIdUUID: IsUUID[CategoryId] = new IsUUID[CategoryId] {
    val _UUID: Iso[UUID, CategoryId] = Iso[UUID, CategoryId](CategoryId.apply)(_.value)
  }

  given itemIdUUID: IsUUID[ItemId] = new IsUUID[ItemId] {
    val _UUID: Iso[UUID, ItemId] = Iso[UUID, ItemId](ItemId.apply)(_.value)
  }

  given orderIdUUID: IsUUID[OrderId] = new IsUUID[OrderId] {
    val _UUID: Iso[UUID, OrderId] = Iso[UUID, OrderId](OrderId.apply)(_.value)
  }

  given userIdUUID: IsUUID[UserId] = new IsUUID[UserId] {
    val _UUID: Iso[UUID, UserId] = Iso[UUID, UserId](UserId.apply)(_.value)
  }
}
