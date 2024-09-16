package ru.orangepigment.pfp.util

import ru.orangepigment.pfp.models.{
  BrandId,
  BrandName,
  CategoryId,
  CategoryName,
  EncryptedPassword,
  ItemDescription,
  ItemId,
  ItemName,
  OrderId,
  PaymentId,
  UserId,
  UserName
}
import skunk.Codec
import skunk.codec.all.*
import squants.market.{ Money, USD }

object sqlcodecs {
  val brandId: Codec[BrandId] =
    uuid.imap[BrandId](BrandId.apply)(_.value)

  val brandName: Codec[BrandName] =
    varchar.imap[BrandName](BrandName.apply)(_.value)

  val categoryId: Codec[CategoryId] =
    uuid.imap[CategoryId](CategoryId.apply)(_.value)

  val categoryName: Codec[CategoryName] =
    varchar.imap[CategoryName](CategoryName.apply)(_.value)

  val itemId: Codec[ItemId] =
    uuid.imap[ItemId](ItemId.apply)(_.value)

  val itemName: Codec[ItemName] =
    varchar.imap[ItemName](ItemName.apply)(_.value)

  val itemDesc: Codec[ItemDescription] =
    varchar.imap[ItemDescription](ItemDescription.apply)(_.value)

  // FixME: save both amount and currency
  val money: Codec[Money] =
    numeric.imap[Money](USD.apply)(_.amount)

  val orderId: Codec[OrderId] =
    uuid.imap[OrderId](OrderId.apply)(_.value)

  val userId: Codec[UserId] =
    uuid.imap[UserId](UserId.apply)(_.value)

  val userName: Codec[UserName] =
    varchar.imap[UserName](UserName.apply)(_.value)

  val paymentId: Codec[PaymentId] =
    uuid.imap[PaymentId](PaymentId.apply)(_.value)

  val encryptedPassword: Codec[EncryptedPassword] =
    varchar.imap[EncryptedPassword](EncryptedPassword.apply)(_.value)
}
