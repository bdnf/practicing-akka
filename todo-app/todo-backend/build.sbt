
name := "todo-backend"

version := "0.1"

scalaVersion := "2.12.8"

val circeVersion = "0.9.3"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "com.typesafe.akka" %% "akka-actor" % "2.5.19",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.19" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19" % Test,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-jawn" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)