package ru.orangepigment.pfp.services

import cats.data.NonEmptyList
import ru.orangepigment.pfp.models.{ CartItem, Order, OrderId, PaymentId, UserId }
import squants.market.Money

trait Orders[F[_]] {
  def get(
      userId: UserId,
      orderId: OrderId
  ): F[Option[Order]]

  def findBy(userId: UserId): F[List[Order]]

  def create(
      userId: UserId,
      paymentId: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId]
}
