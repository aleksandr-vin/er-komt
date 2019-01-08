import sbtbuildinfo.BuildInfoPlugin.autoImport.buildInfoOptions

val Http4sVersion = "0.19.0-M4"
val CirceVersion = "0.10.0"
val Specs2Version = "4.2.0"
val LogbackVersion = "1.2.3"
val Github4sVersion = "0.20.0"

lazy val root = (project in file("."))
  .settings(
    organization := "com.xvygyjau",
    name := "er-komt",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-twirl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test,
      "org.scalatest" %% "scalatest" % "3.0.4" % Test,
      "com.47deg" %% "github4s" % Github4sVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
    scalacOptions ++= Seq("-Ypartial-unification"),
    buildInfoPackage := "com.xvygyjau.erkomt",
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      resolvers,
      libraryDependencies
    ),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson
  )
  .enablePlugins(GitVersioning)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(SbtTwirl)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
)
