name := "akka-queue"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akkaVersion = "2.5.19"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test
  )
}