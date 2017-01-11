name := """twitterstream"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  specs2,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  "org.twitter4j" % "twitter4j-async" % "4.0.4",
  "org.twitter4j" % "twitter4j-stream" % "4.0.4",
  "org.twitter4j" % "twitter4j-media-support" % "4.0.4",
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.14",
  "com.typesafe.play" % "play-streams_2.11" % "2.5.10",
  "com.typesafe.play" % "play_2.11" % "2.5.10"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

fork in run := true
PlayKeys.devSettings := Seq("play.server.http.port" -> "1234")
