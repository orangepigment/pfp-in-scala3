package ru.orangepigment.pfp.services

import java.util.Locale.Category

import ru.orangepigment.pfp.models.{ CategoryId, CategoryName }

trait Categories[F[_]] {
  def findAll: F[List[Category]]

  def create(name: CategoryName): F[CategoryId]
}
