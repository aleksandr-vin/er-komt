package com.xvygyjau.erkomt

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    ErkomtServer.stream[IO].compile.drain.as(ExitCode.Success)
}