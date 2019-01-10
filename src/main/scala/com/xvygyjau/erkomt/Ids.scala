package com.xvygyjau.erkomt

object Ids {
  def md5HashString(s: String): String = {
    // Credits to https://alvinalexander.com/source-code/scala-method-create-md5-hash-of-string
    import java.security.MessageDigest
    import java.math.BigInteger
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1,digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }
}
