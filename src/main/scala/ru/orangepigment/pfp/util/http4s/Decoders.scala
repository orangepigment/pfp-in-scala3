package ru.orangepigment.pfp.util.http4s

import cats.MonadThrow
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.either._
import io.circe.Decoder
import io.github.iltotore.iron._
import monix.newtypes.HasBuilder
import org.http4s.circe._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{ ParseFailure, QueryParamDecoder, Request, Response }

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

trait RefinedRequestDecoder[F[_]: JsonDecoder: MonadThrow] {
  this: Http4sDsl[F] =>

  extension (req: Request[F]) {
    def decodeR[A: Decoder](
        f: A => F[Response[F]]
    ): F[Response[F]] =
      req.asJsonDecode[A].attempt.flatMap {
        case Left(e) =>
          Option(e.getCause) match {
            case Some(c) if c.getMessage.startsWith("Predicate") =>
              BadRequest(c.getMessage)
            case _ =>
              UnprocessableEntity()
          }
        case Right(a) => f(a)
      }
  }
}
