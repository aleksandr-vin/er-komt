package com.xvygyjau.erkomt

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Decoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

trait Quiz[F[_]] {
  def getPhrase: F[UnstressedDaar]
}

case class UnstressedDaar(left: String,
                          quiz: List[String],
                          answer: List[String],
                          right: String)
// http://www.dutchgrammar.com/en/?n=WordOrder.37

object Quizes {
  val all = List(
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
}

object Quiz {
  implicit def apply[F[_]](implicit ev: Quiz[F]): Quiz[F] = ev

  def impl[F[_]: Applicative]: Quiz[F] = new Quiz[F] {
    def getPhrase: F[UnstressedDaar] = {
      Quizes.all.last
    }.pure[F]
  }
}
