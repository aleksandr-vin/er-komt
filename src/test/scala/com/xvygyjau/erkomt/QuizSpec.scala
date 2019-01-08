package com.xvygyjau.erkomt

import cats.effect.IO
import org.http4s._
import org.http4s.headers.Location
import org.http4s.implicits._
import org.scalatest.Matchers

class QuizSpec extends BaseSpec with Matchers {

  {
    lazy val result = getRoot

    it should "redirect" in {
      getRoot.status should be(Status.TemporaryRedirect)
    }

    it should "redirect to quiz" in {
      val header: Option[Location] = result.headers.get(Location)

      header shouldBe defined
      header.get.value should be("quiz")
    }
  }

  {
    lazy val result = retQuiz

    it should "return 200" in {
      result.status should be(Status.Ok)
    }

    it should "return html with phrase" in {
      val body = result.as[String].unsafeRunSync()

      body should include("""</html>""")
      body should include("""</h1>""")
    }
  }

  private[this] val getRoot: Response[IO] = {
    val getHW = Request[IO](Method.GET, Uri.uri("/"))
    val quiz = Quiz.impl[IO]
    ErkomtRoutes.quizRoutes(quiz).orNotFound(getHW).unsafeRunSync()
  }

  private[this] val retQuiz: Response[IO] = {
    val getHW = Request[IO](Method.GET, Uri.uri("/quiz"))
    val quiz = Quiz.impl[IO]
    ErkomtRoutes.quizRoutes(quiz).orNotFound(getHW).unsafeRunSync()
  }

}
