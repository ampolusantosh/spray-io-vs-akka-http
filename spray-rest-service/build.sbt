name := "spray-rest-service"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += Resolver.mavenLocal
retrieveManaged := true

libraryDependencies ++= {
  val sprayVersion = "1.3.3"
  val sprayJsonVersion = "1.3.2"
  val akkaVersion = "2.3.6"
  val specs2Version = "2.3.13"
  val scalaTestVersion = "2.2.5"
  val parserVersion = "1.0.2"
  Seq(
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-json" % sprayJsonVersion,
    "io.spray" %% "spray-testkit" % sprayVersion % Test,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.specs2" %% "specs2-core" % specs2Version % Test,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.scala-lang.modules" %% "scala-parser-combinators" % parserVersion,
    "org.scala-lang" % "scala-library" % "2.11.7",
    "junit" % "junit" % "4.12"
  )
}
