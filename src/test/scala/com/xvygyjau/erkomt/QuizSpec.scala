package com.xvygyjau.erkomt

import cats.effect.IO
import org.http4s._
import org.http4s.headers.{Location, Referer}
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
    lazy val key = quizes.table.keys.head
    lazy val result = getSomeQuiz(key)

    "/quiz/{key}" should "return 200" in {
      result.status should be(Status.Ok)
    }

    it should "return html with phrase" in {
      val body = result.as[String].unsafeRunSync()

      body should include("""</html>""")
      body should include("""</p>""")
    }
  }

  {
    lazy val result = getRandomQuiz(None)

    "/quiz" should "redirect" in {
      result.status should be(Status.TemporaryRedirect)
    }

    it should "return quiz/{key}" in {
      val header: Option[Location] = result.headers.get(Location)

      header shouldBe defined
      header.get.value should startWith("quiz/")
      header.get.value should not be "quiz/"
    }

    "/quiz with referer {key}" should "return different quiz/{key}" in {
      assert(quizes.table.keys.size >= 2)
      val location: Option[Location] = result.headers.get(Location)
      val key = location.get.value.drop(5)
      lazy val resultWithReferer = getRandomQuiz(Some(key))

      resultWithReferer.status should be(Status.TemporaryRedirect)

      val header: Option[Location] = resultWithReferer.headers.get(Location)

      header shouldBe defined
      header.get.value should startWith("quiz/")
      header.get.value should not be "quiz/"
      header.get.value should not be s"quiz/$key"
    }
  }

  lazy val quizes = new Quizes {
    override val all = List(
      UnstressedDaar("Het heeft",
        List("er", "daar"),
        List("er", "daar"),
        "gisteren heel hard geregend"),
      UnstressedDaar(
        "Kun je",
        List("er", "daar"),
        List("daar"),
        "niet tegen, dan kun je maar beter naar vrouwen van andere nationaliteiten kijken")
    )
  }

  private[this] def get(uri: Uri, headers: Headers = Headers.empty): Response[IO] = {
    val getHW = Request[IO](Method.GET, uri, headers = headers)
    val quiz = Quiz.impl[IO](quizes)
    ErkomtRoutes.quizRoutes(quiz).orNotFound(getHW).unsafeRunSync()
  }

  private[this] val getRoot: Response[IO] = {
    get(Uri.uri("/"))
  }

  private[this] val getQuizRoot: Response[IO] = {
    get(Uri.uri("/quiz/"))
  }

  private[this] def getSomeQuiz(key: String): Response[IO] = {
    get(Uri.fromString(s"/quiz/$key").right.get)
  }

  private[this] def getRandomQuiz(skipKey: Option[String]): Response[IO] = skipKey match {
    case None =>
      get(Uri.uri("/quiz"))
    case Some(key) =>
      get(Uri.uri("/quiz"), Headers(Referer(Uri.fromString(s"https://0.0.0.0/quiz/$key").right.get)))
  }

}
