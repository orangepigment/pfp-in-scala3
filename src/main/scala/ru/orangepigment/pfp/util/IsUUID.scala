package ru.orangepigment.pfp.util

import java.util.UUID
import monocle.Iso

trait IsUUID[A] {
  def _UUID: Iso[UUID, A]
}

object IsUUID {
  def apply[A: IsUUID]: IsUUID[A] = summon

  given identityUUID: IsUUID[UUID] = new IsUUID[UUID] {
    val _UUID: Iso[UUID, UUID] = Iso[UUID, UUID](identity)(identity)
  }
}
