package com.xvygyjau.erkomt

import cats.Applicative
import cats.implicits._
import scala.util.Random

trait Quiz[F[_]] {
  def getPhrase(key: String): F[UnstressedDaar]
  def getRandomPhraseKey: F[String]
}

case class UnstressedDaar(left: String,
                          quiz: List[String],
                          answer: List[String],
                          right: String)
// http://www.dutchgrammar.com/en/?n=WordOrder.37

trait Quizes {
  def all: List[UnstressedDaar]

  lazy val table: Map[String, UnstressedDaar] = all.map {
    case x @ UnstressedDaar(left, _, _, right) =>
      Ids.md5HashString(left ++ right) -> x
  }.toMap
}

object Quiz {
  implicit def apply[F[_]](implicit ev: Quiz[F]): Quiz[F] = ev

  def impl[F[_]: Applicative](quizes: Quizes): Quiz[F] = new Quiz[F] {
    def getPhrase(key: String): F[UnstressedDaar] = {
      quizes.table(key)
    }.pure[F]

    def getRandomPhraseKey: F[String] = {
      Random.shuffle(quizes.table.keys).head
    }.pure[F]
  }
}
