package ru.orangepigment.pfp.conf

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.collection.*
import monix.newtypes.NewtypeWrapped

type PaymentURI = PaymentURI.Type
object PaymentURI extends NewtypeWrapped[String :| Not[Empty]]

type PaymentConfig = PaymentConfig.Type
object PaymentConfig extends NewtypeWrapped[PaymentURI]

type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.Type
object JwtAccessTokenKeyConfig extends NewtypeWrapped[String :| Not[Empty]]

type JwtSecretKeyConfig = JwtSecretKeyConfig.Type
object JwtSecretKeyConfig extends NewtypeWrapped[String :| Not[Empty]]
