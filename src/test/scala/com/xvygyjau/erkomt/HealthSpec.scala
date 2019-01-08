package com.xvygyjau.erkomt

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.scalatest.Matchers

class HealthSpec extends BaseSpec with Matchers {

  it should "return 200" in {
    retHealth.status should be (Status.Ok)
  }

  it should "return health report" in {
    val result = retHealth.as[String].unsafeRunSync()
    result should include (""""builtAtString"""")
    result should include (""""version"""")
    result should include (""""name"""")
  }

  private[this] val retHealth: Response[IO] = {
    val getHW = Request[IO](Method.GET, Uri.uri("/health"))
    val health = Health.impl[IO]
    ErkomtRoutes.healthRoutes(health).orNotFound(getHW).unsafeRunSync()
  }

}
