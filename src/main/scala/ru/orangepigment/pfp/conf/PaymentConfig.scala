package ru.orangepigment.pfp.conf

import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.collection._
import monix.newtypes.NewtypeWrapped

type PaymentURI = PaymentURI.Type
object PaymentURI extends NewtypeWrapped[String :| Not[Empty]]

type PaymentConfig = PaymentConfig.Type
object PaymentConfig extends NewtypeWrapped[PaymentURI]
