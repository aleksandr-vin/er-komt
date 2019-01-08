package com.xvygyjau.erkomt

import cats.effect.Sync
import cats.implicits._
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import play.twirl.api.Html
import org.http4s.twirl._

object ErkomtRoutes {

  def jokeRoutes[F[_]: Sync](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        for {
          joke <- J.get
          resp <- Ok(joke)
        } yield resp
    }
  }

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
  }

  def healthRoutes[F[_]: Sync](H: Health[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "health" =>
        for {
          report <- H.health
          resp <- Ok(report)
        } yield resp
    }
  }

  def quizRoutes[F[_]: Sync](H: Quiz[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "quiz" =>
        for {
          phrase <- H.getPhrase
          resp <- Ok(erkomt.html.phrase(phrase))
        } yield resp
      case GET -> Root =>
        TemporaryRedirect(Location(Uri.uri("quiz")))
    }
  }
}