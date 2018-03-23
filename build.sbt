import sbt.Keys.libraryDependencies
import sbtassembly.PathList

name := "NoLimitJarvis"
version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10" % Test,
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.play" %% "play-json" % "2.6.2",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.17.0",
  "ch.megard" %% "akka-http-cors" % "0.2.1"
)

mainClass in assembly := Some("id.nolimit.jarvis.Main")