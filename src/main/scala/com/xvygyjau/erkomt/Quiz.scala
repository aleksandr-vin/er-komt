package com.xvygyjau.erkomt

import cats.Applicative
import cats.implicits._
import io.circe.{Decoder, Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

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

object Quizes {
  lazy val all = List(
    UnstressedDaar("Het heeft",
                   List("er", "daar"),
                   List("er", "daar"),
                   "gisteren heel hard geregend"),
    UnstressedDaar("Zij hebben",
                   List("er", "daar"),
                   List("er", "daar"),
                   "zes jaar lang op school gezeten"),
    UnstressedDaar(
      "Kun je",
      List("er", "daar"),
      List("daar"),
      "niet tegen, dan kun je maar beter naar vrouwen van andere nationaliteiten kijken")
  )

  val table: Map[String, UnstressedDaar] = all.map {
    case x @ UnstressedDaar(left, _, _, right) =>
      Ids.md5HashString(left ++ right) -> x
  }.toMap
}

object Quiz {
  implicit def apply[F[_]](implicit ev: Quiz[F]): Quiz[F] = ev

  def impl[F[_]: Applicative]: Quiz[F] = new Quiz[F] {
    def getPhrase(key: String): F[UnstressedDaar] = {
      Quizes.table(key)
    }.pure[F]

    def getRandomPhraseKey: F[String] = {
      Random.shuffle(Quizes.table.keys).head
    }.pure[F]
  }
}
