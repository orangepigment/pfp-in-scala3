package ru.orangepigment.pfp.services

import ru.orangepigment.pfp.models.AppStatus

trait HealthCheck[F[_]] {
  def status: F[AppStatus]
}
