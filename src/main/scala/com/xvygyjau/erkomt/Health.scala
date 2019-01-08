package com.xvygyjau.erkomt

import cats.Applicative
import cats.implicits._

trait Health[F[_]]{
  def health: F[String]
}

object Health {
  implicit def apply[F[_]](implicit ev: Health[F]): Health[F] = ev

  def impl[F[_]: Applicative]: Health[F] = new Health[F]{
    def health: F[String] =
      BuildInfo.toJson.pure[F]
  }
}