package ru.orangepigment.pfp.services

import ru.orangepigment.pfp.models.{ Category, CategoryId, CategoryName }

trait Categories[F[_]] {
  def findAll: F[List[Category]]

  def create(name: CategoryName): F[CategoryId]
}
