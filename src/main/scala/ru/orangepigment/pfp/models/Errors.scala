package ru.orangepigment.pfp.models

import scala.util.control.NoStackTrace

import cats.{ Eq, Show }
import cats.derived._
import io.circe.Codec

object Errors {

  case object EmptyCartError extends NoStackTrace derives Show

  sealed trait OrderOrPaymentError extends NoStackTrace derives Show {
    def cause: String
  }

  case class OrderError(cause: String)   extends OrderOrPaymentError derives Show, Eq
  case class PaymentError(cause: String) extends OrderOrPaymentError derives Show, Eq

  case class CartNotFound(userId: UserId) extends NoStackTrace derives Codec.AsObject

  case class UserNotFound(username: UserName)    extends NoStackTrace
  case class UserNameInUse(username: UserName)   extends NoStackTrace derives Show
  case class InvalidPassword(username: UserName) extends NoStackTrace

}
