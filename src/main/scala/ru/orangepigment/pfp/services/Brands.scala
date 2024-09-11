package ru.orangepigment.pfp.services

import ru.orangepigment.pfp.models.{ Brand, BrandId, BrandName }

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[BrandId]
}
