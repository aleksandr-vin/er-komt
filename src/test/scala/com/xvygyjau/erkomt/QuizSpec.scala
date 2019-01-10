package com.xvygyjau.erkomt

import cats.effect.IO
import org.http4s._
import org.http4s.headers.Location
import org.http4s.implicits._
import org.scalatest.Matchers

class QuizSpec extends BaseSpec with Matchers {

  {
    lazy val result = getRoot

    "/" should "redirect" in {
      getRoot.status should be(Status.TemporaryRedirect)
    }

    it should "redirect to quiz" in {
      val header: Option[Location] = result.headers.get(Location)

      header shouldBe defined
      header.get.value should be("quiz")
    }
  }

  {
    lazy val result = getQuizRoot

    "/quiz/" should "redirect" in {
      getRoot.status should be(Status.TemporaryRedirect)
    }

    it should "redirect to .." in {
      val header: Option[Location] = result.headers.get(Location)

      header shouldBe defined
      header.get.value should be("..")
    }
  }

  {
    lazy val result = getSomeQuiz

    "/quiz/b432abf9e90d7abdbda8db220f0e5988" should "return 200" in {
      result.status should be(Status.Ok)
    }

    it should "return html with phrase" in {
      val body = result.as[String].unsafeRunSync()

      body should include("""</html>""")
      body should include("""</p>""")
    }
  }

  {
    lazy val result = getRandomQuiz

    "/quiz" should "redirect" in {
      result.status should be(Status.TemporaryRedirect)
    }

    it should "return to quiz/{key}" in {
      val header: Option[Location] = result.headers.get(Location)

      header shouldBe defined
      header.get.value should startWith("quiz/")
      header.get.value should not be "quiz/"
    }
  }

  private[this] def get(uri: Uri): Response[IO] = {
    val getHW = Request[IO](Method.GET, uri)
    val quiz = Quiz.impl[IO]
    ErkomtRoutes.quizRoutes(quiz).orNotFound(getHW).unsafeRunSync()
  }

  private[this] val getRoot: Response[IO] = {
    get(Uri.uri("/"))
  }

  private[this] val getQuizRoot: Response[IO] = {
    get(Uri.uri("/quiz/"))
  }

  private[this] val getSomeQuiz: Response[IO] = {
    get(Uri.uri("/quiz/b432abf9e90d7abdbda8db220f0e5988"))
  }

  private[this] def getRandomQuiz(): Response[IO] = {
    get(Uri.uri("/quiz"))
  }

}
