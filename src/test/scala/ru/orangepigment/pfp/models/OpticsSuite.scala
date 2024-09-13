package ru.orangepigment.pfp.models

import java.util.UUID

import monocle.law.discipline.IsoTests
import org.scalacheck.{ Arbitrary, Cogen, Gen }
import ru.orangepigment.pfp.util.IsUUID
import ru.orangepigment.pfp.utils.generators.brandIdGen
import weaver.FunSuite
import weaver.discipline.Discipline

object OpticsSuite extends FunSuite with Discipline {
  given Arbitrary[Status] =
    Arbitrary(Gen.oneOf(Status.Okay, Status.Unreachable))

  implicit val brandIdArb: Arbitrary[BrandId] =
    Arbitrary(brandIdGen)

  given Cogen[BrandId] =
    Cogen[UUID].contramap[BrandId](_.value)

  checkAll("Iso[Status._Bool]", IsoTests(Status._Bool))
  // bonus checks
  checkAll("IsUUID[UUID]", IsoTests(IsUUID[UUID]._UUID))
  checkAll("IsUUID[BrandId]", IsoTests(IsUUID[BrandId]._UUID))
}
