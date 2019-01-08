package com.xvygyjau.erkomt

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Decoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

trait Quiz[F[_]]{
  def getPhrase: F[Quiz.Phrase]
}

object Quiz {
  implicit def apply[F[_]](implicit ev: Quiz[F]): Quiz[F] = ev

  final case class Phrase(phrase: String) extends AnyVal

  object Phrase {
    implicit val phraseEncoder: Encoder[Phrase] = new Encoder[Phrase] {
      final def apply(a: Phrase): Json = Json.obj(
        ("message", Json.fromString(a.phrase))
      )
    }
    implicit def phraseEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Phrase] =
      jsonEncoderOf[F, Phrase]
  }

  def impl[F[_]: Applicative]: Quiz[F] = new Quiz[F]{
    def getPhrase: F[Quiz.Phrase] = {
      Phrase("__ komt een man")
    }.pure[F]
  }
}