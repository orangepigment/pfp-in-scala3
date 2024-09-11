import Dependencies.*

ThisBuild / scalaVersion := "3.5.0"
ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / organization := "ru.orangepigment"
ThisBuild / organizationName := "orangepigment"

ThisBuild / evictionErrorLevel := Level.Warn
Global / semanticdbEnabled := true

lazy val root = (project in file("."))
  .settings(
    name := "pfp-in-scala3",
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.catsRetry,
      Libraries.kittens,
      Libraries.circeCore,
      Libraries.circeParser,
      Libraries.circeRefined,
      Libraries.cirisCore,
      Libraries.cirisRefined,
      Libraries.fs2,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sCirce,
      Libraries.http4sJwtAuth,
      Libraries.ironCore,
      Libraries.ironCats,
      Libraries.ironCirce,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.monocleCore,
      Libraries.monixNewtypesCore,
      Libraries.monixNewtypesCirce,
      Libraries.redis4catsEffects,
      Libraries.redis4catsLog4cats,
      Libraries.skunkCore,
      Libraries.skunkCirce,
      Libraries.squants
    )
  )

addCommandAlias("lint", ";scalafmtAll ;scalafixAll --rules OrganizeImports")
