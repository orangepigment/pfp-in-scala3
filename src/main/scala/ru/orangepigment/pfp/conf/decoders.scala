package ru.orangepigment.pfp.conf

import cats.syntax.either.*
import ciris.ConfigDecoder
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*

object decoders {

  given ConfigDecoder[String, String :| Not[Empty]] =
    ConfigDecoder[String].mapOption("String :| Not[Empty]")(s =>
      Either.catchNonFatal(s.refineUnsafe[Not[Empty]]).toOption
    )

}
