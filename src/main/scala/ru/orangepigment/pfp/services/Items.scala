package ru.orangepigment.pfp.services

import ru.orangepigment.pfp.models.{ BrandName, CreateItem, Item, ItemId, UpdateItem }

trait Items[F[_]] {
  def findAll: F[List[Item]]

  def findBy(brand: BrandName): F[List[Item]]

  def findById(itemId: ItemId): F[Option[Item]]

  def create(item: CreateItem): F[ItemId]

  def update(item: UpdateItem): F[Unit]
}
