
name := "wordcount"

version := "0.1"

scalaVersion := "2.13.6"

lazy val `wordcount-scala-solution` = (project in file("wordcount-scala-solution"))
  .dependsOn(`wordcount-java`)
  .settings(libraryDependencies ++= Seq(
    "org.scalactic" %% "scalactic" % "3.2.9",
    "org.scalatest" %% "scalatest" % "3.2.9" % "test"
  ))

lazy val `wordcount-java` = (project in file("wordcount-java"))
  .settings(Compile / javaSource := baseDirectory.value / "src")