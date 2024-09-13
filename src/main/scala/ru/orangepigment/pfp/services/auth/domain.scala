package ru.orangepigment.pfp.services.auth

import monix.newtypes.*

import scala.concurrent.duration.FiniteDuration

type TokenExpiration = TokenExpiration.Type
object TokenExpiration extends NewtypeWrapped[FiniteDuration]
