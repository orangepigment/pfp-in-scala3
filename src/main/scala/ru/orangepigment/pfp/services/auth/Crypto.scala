package ru.orangepigment.pfp.services.auth

import ru.orangepigment.pfp.models.{ EncryptedPassword, Password }

trait Crypto {
  def encrypt(value: Password): EncryptedPassword
  def decrypt(value: EncryptedPassword): Password
}
