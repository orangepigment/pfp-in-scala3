package ru.orangepigment.pfp.conf

import cats.syntax.either.*
import ciris.ConfigDecoder

enum AppEnvironment:
  case Test
  case Prod

object AppEnvironment {
  given ConfigDecoder[String, AppEnvironment] =
    ConfigDecoder[String].mapOption("ru.orangepigment.pfp.conf.AppEnvironment")(s =>
      Either.catchNonFatal(AppEnvironment.valueOf(s.toLowerCase.capitalize)).toOption
    )
}
