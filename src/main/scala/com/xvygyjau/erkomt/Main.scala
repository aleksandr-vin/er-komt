package com.xvygyjau.erkomt

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def run(args: List[String]) =
    ErkomtServer.stream[IO].compile.drain.as(ExitCode.Success)
}