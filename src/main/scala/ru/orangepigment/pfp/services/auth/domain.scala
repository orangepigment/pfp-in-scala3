package ru.orangepigment.pfp.services.auth

import scala.concurrent.duration.FiniteDuration

import monix.newtypes._

type TokenExpiration = TokenExpiration.Type
object TokenExpiration extends NewtypeWrapped[FiniteDuration]
