package ru.orangepigment.pfp.util.http4s

import cats.syntax.either.*
import io.github.iltotore.iron.*
import monix.newtypes.HasBuilder
import org.http4s.{ ParseFailure, QueryParamDecoder }

object RefinedParamDecoder {
  inline def derive[In: QueryParamDecoder, Out](using
      constraint: Constraint[In, Out]
  ): QueryParamDecoder[In :| Out] =
    QueryParamDecoder[In].emap(t => t.refineEither[Out].leftMap(s => ParseFailure(s, s)))

}

trait NewtypeParamDecoder {
  given newtypeParamDecoder[In: QueryParamDecoder, Out](using
      builder: HasBuilder.Aux[Out, In]
  ): QueryParamDecoder[Out] =
    QueryParamDecoder[In].emap(t => builder.build(t).leftMap(f => ParseFailure(f.toReadableString, f.toReadableString)))

}
