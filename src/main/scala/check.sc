import scala.util.Random

object quizes {
  val table = List("a", "b", "c", "d").map(x => x -> x).toMap
}

val skipKey = "d"

val salt = "askdf"

Random.setSeed(salt.hashCode)
val randomPhrases = Random.shuffle(quizes.table.keys).toList

randomPhrases.span(!skipKey.contains(_)) match {
  case (passed, List()) => passed.head
  case (passed, _ :: List()) => passed.head
  case (_, _ :: tail) => tail.head
}


(1 to 4).map(_.toString)