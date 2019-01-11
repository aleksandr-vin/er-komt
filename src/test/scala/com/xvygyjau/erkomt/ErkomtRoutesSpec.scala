package com.xvygyjau.erkomt

import org.http4s.{Headers, Uri}
import org.http4s.headers.Referer
import org.scalatest.Matchers

class ErkomtRoutesSpec extends BaseSpec with Matchers {

  "getRefererQuizKey" should "not return key without Referer" in {
    ErkomtRoutes.getRefererQuizKey(Headers.empty) shouldBe None
  }

  it should "not return key if it is an empty string" in {
    val key = getRefererQuizKey(Uri.uri("https://0.0.0.0/quiz/"))
    key shouldBe None
  }

  it should "not return key if it is not present" in {
    val key = getRefererQuizKey(Uri.uri("https://0.0.0.0/quiz"))
    key shouldBe None
  }

  it should "return referer key if present" in {
    val key = getRefererQuizKey(Uri.uri("https://0.0.0.0/quiz/abc"))
    key shouldBe defined
    key.get shouldBe "abc"
  }

  private def getRefererQuizKey(uri: Uri) = ErkomtRoutes.getRefererQuizKey(Headers(Referer(uri)))
}
