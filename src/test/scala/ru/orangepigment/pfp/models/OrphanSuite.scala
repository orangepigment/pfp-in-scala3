package ru.orangepigment.pfp.models

import cats.kernel.laws.discipline.MonoidTests
import org.scalacheck.Arbitrary
import ru.orangepigment.pfp.utils.generators.moneyGen
import squants.market.Money
import weaver.FunSuite
import weaver.discipline.Discipline
import ru.orangepigment.pfp.models.OrphanInstances.given

object OrphanSuite extends FunSuite with Discipline {
  given Arbitrary[Money] = Arbitrary(moneyGen)

  checkAll("Monoid[Money]", MonoidTests[Money].monoid)
}
