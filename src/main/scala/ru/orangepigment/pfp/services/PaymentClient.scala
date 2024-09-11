package ru.orangepigment.pfp.services

import ru.orangepigment.pfp.models.{ Payment, PaymentId }

trait PaymentClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}
