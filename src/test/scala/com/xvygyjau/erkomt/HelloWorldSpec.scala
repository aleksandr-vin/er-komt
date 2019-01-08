package com.xvygyjau.erkomt

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.scalatest.Matchers

class HelloWorldSpec extends BaseSpec with Matchers {

  it should "return 200" in {
    retHelloWorld.status should be (Status.Ok)
  }

  it should "return hello world" in {
    retHelloWorld.as[String].unsafeRunSync() should include ("{\"message\":\"Hello, world\"}")
  }

  private[this] val retHelloWorld: Response[IO] = {
    val getHW = Request[IO](Method.GET, Uri.uri("/hello/world"))
    val helloWorld = HelloWorld.impl[IO]
    ErkomtRoutes.helloWorldRoutes(helloWorld).orNotFound(getHW).unsafeRunSync()
  }

}
