package ru.orangepigment.pfp.utils

import java.util.UUID

import io.github.iltotore.iron._
import org.scalacheck.Gen
import ru.orangepigment.pfp.models._
import squants.market.{ Money, USD }

object generators {

  val nonEmptyStringGen: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  def nesGen[A](f: String => A): Gen[A] =
    nonEmptyStringGen.map(f)

  def idGen[A](f: UUID => A): Gen[A] =
    Gen.uuid.map(f)

  val brandIdGen: Gen[BrandId] =
    idGen(BrandId.apply)

  val brandNameGen: Gen[BrandName] =
    nesGen(BrandName.apply)

  val brandGen: Gen[Brand] =
    for {
      i <- brandIdGen
      n <- brandNameGen
    } yield Brand(i, n)

  val categoryIdGen: Gen[CategoryId] =
    idGen(CategoryId.apply)

  val categoryNameGen: Gen[CategoryName] =
    nesGen(CategoryName.apply)

  val categoryGen: Gen[Category] =
    for {
      i <- categoryIdGen
      n <- categoryNameGen
    } yield Category(i, n)

  val moneyGen: Gen[Money] =
    Gen.posNum[Long].map(n => USD(BigDecimal(n)))

  val itemIdGen: Gen[ItemId] =
    idGen(ItemId.apply)

  val itemNameGen: Gen[ItemName] =
    nesGen(ItemName.apply)

  val itemDescriptionGen: Gen[ItemDescription] =
    nesGen(ItemDescription.apply)

  val itemGen: Gen[Item] =
    for {
      i <- itemIdGen
      n <- itemNameGen
      d <- itemDescriptionGen
      p <- moneyGen
      b <- brandGen
      c <- categoryGen
    } yield Item(i, n, d, p, b, c)

  val quantityGen: Gen[Quantity] =
    Gen.posNum[Int].map(Quantity.apply)

  val cartItemGen: Gen[CartItem] =
    for {
      i <- itemGen
      q <- quantityGen
    } yield CartItem(i, q)

  val cartTotalGen: Gen[CartTotal] =
    for {
      i <- Gen.nonEmptyListOf(cartItemGen)
      t <- moneyGen
    } yield CartTotal(i, t)

  val itemMapGen: Gen[(ItemId, Quantity)] =
    for {
      i <- itemIdGen
      q <- quantityGen
    } yield i -> q

  val cartGen: Gen[Cart] =
    Gen.nonEmptyMap(itemMapGen).map(Cart.apply)

  val cardNameGen: Gen[CardHolderName] =
    Gen
      .stringOf(
        Gen.oneOf(('a' to 'z') ++ ('A' to 'Z'))
      )
      .map(CardHolderName.apply)

  private def sized(size: Int): Gen[Long] = {
    def go(s: Int, acc: Long): Gen[Long] =
      Gen.oneOf(1 to 9).flatMap { n =>
        if (s == size) acc
        else go(s + 1, 10 * acc + n)
      }

    go(0, 0L)
  }

  val cardGen: Gen[Card] =
    for {
      n <- cardNameGen
      u <- sized(16).map(l => CardNumber(l.refineUnsafe))
      x <- sized(4).map(l => CardExpiration(l.toString.refineUnsafe))
      c <- sized(3).map(l => CVV(l.toInt.refineUnsafe))
    } yield Card(n, u, x, c)

  val userIdGen: Gen[UserId] =
    idGen(UserId.apply)

  val userNameGen: Gen[UserName] =
    nesGen(UserName.apply)

  val userGen: Gen[User] =
    for {
      i <- userIdGen
      n <- userNameGen
    } yield User(i, n)

  val commonUserGen: Gen[CommonUser] =
    userGen.map(CommonUser(_))

  val paymentIdGen: Gen[PaymentId] =
    idGen(PaymentId.apply)

  val paymentGen: Gen[Payment] =
    for {
      i <- userIdGen
      m <- moneyGen
      c <- cardGen
    } yield Payment(i, m, c)

  val orderIdGen: Gen[OrderId] =
    idGen(OrderId.apply)
}
