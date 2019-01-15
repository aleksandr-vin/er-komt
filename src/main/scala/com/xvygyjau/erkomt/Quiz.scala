package com.xvygyjau.erkomt

import java.time.Instant

import cats.Applicative
import cats.implicits._

import scala.util.Random

trait Quiz[F[_]] {
  def getPhrase(key: String): F[UnstressedDaar]
  def getRandomPhraseKey(skipKey: Option[String],
                         sortSalt: Option[Int]): F[String]
  def getPreviousPhraseKey(key: String,
                           sortSalt: Option[Int]): F[String]
}

case class Cite(url: String, name: String, site: String)

case class UnstressedDaar(left: String,
                          quiz: List[String],
                          answer: List[String],
                          right: String,
                          cite: Cite)
// http://www.dutchgrammar.com/en/?n=WordOrder.37

trait Quizes {
  def all: List[UnstressedDaar]

  lazy val table: Map[String, UnstressedDaar] = all.map {
    case x @ UnstressedDaar(left, _, _, right, _) =>
      Ids.md5HashString(left ++ right) -> x
  }.toMap
}

object Quiz {
  implicit def apply[F[_]](implicit ev: Quiz[F]): Quiz[F] = ev

  def impl[F[_]: Applicative](quizes: Quizes): Quiz[F] = new Quiz[F] {
    def getPhrase(key: String): F[UnstressedDaar] = {
      quizes.table(key)
    }.pure[F]

    def getRandomPhraseKey(skipKey: Option[String],
                           sortSalt: Option[Int]): F[String] = {
      for {
        _ <- skipKey
        seed <- sortSalt
      } yield Random.setSeed(seed)
      val keys: List[String] = quizes.table.keys.toList
      val randomPhrases = Random.shuffle(keys)
      randomPhrases.span(!skipKey.contains(_)) match {
        case (passed, List())      => passed.head
        case (passed, _ :: List()) => passed.head
        case (_, _ :: tail)        => tail.head
      }
    }.pure[F]

    def getPreviousPhraseKey(key: String,
                             sortSalt: Option[Int]): F[String] = {
      for {
        seed <- sortSalt
      } yield Random.setSeed(seed)
      val keys: List[String] = quizes.table.keys.toList
      val randomPhrases = Random.shuffle(keys)
      randomPhrases.reverse.span(key.compareTo(_) != 0) match {
        case (passed, List())      => passed.head
        case (passed, _ :: List()) => passed.head
        case (_, _ :: tail)        => tail.head
      }
    }.pure[F]
  }
}
