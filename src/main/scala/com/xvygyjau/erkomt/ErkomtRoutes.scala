package com.xvygyjau.erkomt

import cats.effect.{ContextShift, IO, Sync}
import cats.implicits._
import org.http4s.{HttpRoutes, Request, StaticFile, Uri}
import org.http4s.dsl._
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.http4s.twirl._
import java.util.concurrent._
import scala.concurrent.ExecutionContext

object ErkomtRoutes {

  def jokeRoutes[F[_]: Sync](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
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
    val dsl = new Http4sDsl[F] {}
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
    val dsl = new Http4sDsl[F] {}
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
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "quiz" =>
        for {
          key <- H.getRandomPhraseKey
          resp <- Uri
            .fromString(s"quiz/$key")
            .fold(
              { _ => NotFound() },
              { uri => TemporaryRedirect(Location(uri)) }
            )
        } yield resp
      case GET -> Root / "quiz" / key if !key.isEmpty =>
        for {
          phrase <- H.getPhrase(key)
          resp <- Ok(erkomt.html.phrase(phrase))
        } yield resp
      case GET -> Root / "quiz" / "" =>
        TemporaryRedirect(Location(Uri.uri("..")))
      case GET -> Root =>
        TemporaryRedirect(Location(Uri.uri("quiz")))
    }
  }

  def staticFilesRoutes: HttpRoutes[IO] = {

    val blockingEc =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    def static(file: String, request: Request[IO]) =
      StaticFile
        .fromResource("/static/" + file, blockingEc, Some(request))
        .getOrElseF(NotFound())

    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] {
      case request @ GET -> Root / "static" / path
          if List(".js", ".css", ".map", ".html", ".webm").exists(
            path.endsWith) =>
        static(path, request)
    }
  }
}
