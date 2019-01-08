package com.xvygyjau.erkomt

import cats.effect.{
  ConcurrentEffect,
  Timer,
  ContextShift
}
import cats.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import fs2.Stream
import scala.concurrent.ExecutionContext.global
import org.http4s.server.middleware._

object ErkomtServer {

  lazy val port = sys.env.getOrElse("PORT", "8080").toInt

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F],
                                     C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)
      healthAlg = Health.impl[F]

      // Combine Service Routes into an HttpApp
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        ErkomtRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          ErkomtRoutes.jokeRoutes[F](jokeAlg) <+>
          ErkomtRoutes.healthRoutes[F](healthAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger(true, true)(GZip(httpApp))

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
