package ru.orangepigment.pfp.util

import scala.compiletime.constValue

import io.github.iltotore.iron._
import io.github.iltotore.iron.compileTime._

final class NumericLength[V <: Int]

trait NumericLengthConstraint[A, V <: Int] extends Constraint[A, NumericLength[V]] {
  override inline def message: String = "Number of digits must be  " + stringValue[V]

  given NumericLengthConstraint[Int, V] with
    override inline def test(value: Int): Boolean = value.toString.length == constValue[V]

  given NumericLengthConstraint[Long, V] with
    override inline def test(value: Long): Boolean = value.toString.length == constValue[V]
}
