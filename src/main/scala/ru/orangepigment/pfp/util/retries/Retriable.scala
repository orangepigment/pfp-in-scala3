package ru.orangepigment.pfp.util.retries

import cats.Show
import cats.derived.*

enum Retriable derives Show {
  case Orders   extends Retriable
  case Payments extends Retriable
}
