package com.xvygyjau.erkomt

import cats.effect.IO
import org.http4s._
import org.http4s.headers.{AgentProduct, Location, Referer, `User-Agent`}
import org.http4s.implicits._
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually

class QuizSpec extends BaseSpec with Matchers with Eventually {

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

    assert(quizes.table.keys.size >= 2)
    val location: Option[Location] = result.headers.get(Location)


    it should "every time return different quiz/{key}" in {
      // Using eventually to bypass the real random distribution on small set of quizes
      eventually {
        lazy val result2ndNone = getRandomQuiz(None)
        val location2ndNone: Option[Location] =
          result2ndNone.headers.get(Location)
        location2ndNone shouldBe defined
        location2ndNone.get.value should startWith("quiz/")
        location2ndNone.get.value should not be "quiz/"
        location2ndNone.get.value should not be location.get.value
      }
    }

    assert(quizes.table.keys.size >= 2)
    val key = location.get.value.drop(5)
    lazy val result2 = getRandomQuiz(Some(key))

    "/quiz with referer {key}" should "redirect" in {
      result2.status should be(Status.TemporaryRedirect)
    }

    val location2: Option[Location] = result2.headers.get(Location)

    it should "return different quiz/{key}" in {
      location2 shouldBe defined
      location2.get.value should startWith("quiz/")
      location2.get.value should not be "quiz/"
      location2.get.value should not be s"quiz/$key"
    }

    assert(quizes.table.keys.size >= 3)
    lazy val result3 = getRandomQuiz(Some(key))

    it should "constantly return same quiz/{key}" in {
      val location3: Option[Location] = result3.headers.get(Location)
      location3 shouldBe defined
      location3.get.value should be(location2.get.value)
    }
  }

  object FakeQuizes {
    def apply(chars: String): Quizes = new Quizes {
      override val all = chars
        .map(_.toString)
        .map(a =>
          UnstressedDaar(a, List("er"), List("er"), a, Cite("", "", "")))
        .toList
    }
  }

  lazy val quizes = FakeQuizes("abcd")

  private[this] def get(uri: Uri,
                        headers: Headers = Headers.empty): Response[IO] = {
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

  private[this] def getRandomQuiz(skipKey: Option[String]): Response[IO] =
    skipKey match {
      case None =>
        get(Uri.uri("/quiz"), Headers(`User-Agent`(AgentProduct("test"))))
      case Some(key) =>
        get(Uri.uri("/quiz"),
            Headers(
              Referer(Uri.fromString(s"https://0.0.0.0/quiz/$key").right.get),
              `User-Agent`(AgentProduct("test"))))
    }

}
