import sbt.Defaults.testSettings

name := "wordcount"

version := "0.1"

scalaVersion := "2.13.6"

val AkkaVersion = "2.6.15"

lazy val `wordcount-scala-solution` = (project in file("wordcount-scala-solution"))
  .dependsOn(`wordcount-java`)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    "org.scalactic" %% "scalactic" % "3.2.9",
    "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    "org.scalamock" %% "scalamock" % "5.1.0" % Test
  ))

lazy val `wordcount-java` = (project in file("wordcount-java"))
  .settings(Compile / javaSource := baseDirectory.value / "src")