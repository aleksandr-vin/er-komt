package com.xvygyjau.erkomt

import java.time.Instant
import java.util.concurrent._

import cats.effect.{ContextShift, Sync}
import cats.implicits._
import org.http4s.dsl._
import org.http4s.headers.Location
import org.http4s.twirl._
import org.http4s.{Headers, HttpRoutes, Request, StaticFile, Uri, headers}

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
      case GET -> Root =>
        TemporaryRedirect(Location(Uri.uri("quiz")))
      case request @ GET -> Root / "quiz" =>
        val refKey = getRefererQuizKey(request.headers)
        val sortSalt = getSortSalt(request)
        for {
          key <- H.getRandomPhraseKey(refKey, sortSalt)
          resp <- Uri
            .fromString(s"quiz/$key")
            .fold(
              { _ =>
                NotFound()
              }, { uri =>
                TemporaryRedirect(Location(uri))
              }
            )
        } yield resp
      case GET -> Root / "quiz" / "" =>
        TemporaryRedirect(Location(Uri.uri("..")))
      case GET -> Root / "quiz" / key if !key.isEmpty =>
        for {
          phrase <- H.getPhrase(key)
          resp <- Ok(erkomt.html.phrase(phrase, key))
        } yield resp
      case GET -> Root / "quiz" / key / "notes" if !key.isEmpty =>
        for {
          phrase <- H.getPhrase(key)
          resp <- Ok(erkomt.html.notes(phrase, key))
        } yield resp
    }
  }

  def staticFilesRoutes[F[_]: Sync](
      implicit C: ContextShift[F]): HttpRoutes[F] = {

    val blockingEc =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case request @ GET -> Root / "static" / path
          if List(".js", ".css", ".map", ".html", ".webm", ".png", ".ico")
            .exists(path.endsWith) =>
        StaticFile
          .fromResource("/static/" + path, blockingEc, Some(request))
          .getOrElseF(NotFound())
    }
  }

  private[erkomt] def getRefererQuizKey(
      requestHeaders: Headers): Option[String] = {
    headers.Referer
      .from(requestHeaders)
      .map(_.uri.path.toString.toList)
      .collect {
        case '/' :: 'q' :: 'u' :: 'i' :: 'z' :: '/' :: (key: List[Char])
            if key.nonEmpty =>
          key.mkString
      }
  }

  private[erkomt] def getSortSalt[F[_]: Sync](
      request: Request[F]): Option[Int] = {
    val traits = List(request.remoteAddr,
                      headers.`User-Agent`
                        .from(request.headers))
    traits.flatten match {
      case List() => None
      case l =>
        // Making different random shuffling every hour for every "user"
        val distinctHours = Some(Instant.now.toEpochMilli / 1000 / 60 / 60)
        Some((distinctHours :: l).hashCode())
    }
  }
}
