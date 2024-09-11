package ru.orangepigment.pfp.models

import io.circe.Encoder
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceEncoder
import monocle.Iso

type RedisStatus = RedisStatus.Type

object RedisStatus extends NewtypeWrapped[String] with DerivedCirceEncoder

type PostgresStatus = PostgresStatus.Type

object PostgresStatus extends NewtypeWrapped[String] with DerivedCirceEncoder

case class AppStatus(
    redis: RedisStatus,
    postgres: PostgresStatus
) derives Encoder

enum Status {
  case Okay
  case Unreachable

  val _Bool: Iso[Status, Boolean] =
    Iso[Status, Boolean] {
      case Okay        => true
      case Unreachable => false
    }(if (_) Okay else Unreachable)
  implicit val jsonEncoder: Encoder[Status] =
    Encoder.forProduct1("status")(_.toString)
}
