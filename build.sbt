import Dependencies._

name := """scrubber-play-twitter4j"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++=
      Seq(
        cache,
        specs2,
        ws
      )
  )
  .settings(
    libraryDependencies ++=
      compileScope(
        kamonCore,
        kamonScala,
        kamonAkka,
        kamonSysMetrics,
        kamonLogReporter,
        kamonStatsd,
        twitter4JCore,
        twitter4JAsync,
        twitter4JStream,
        twitter4JMediaSuport,
        akkaStream,
        playStream,
        playCore
      )
  )

scalaVersion := "2.11.7"

resolvers ++= Dependencies.resolutionRepos

fork in run := true

//This is where we set the port that we will use for the application
PlayKeys.devSettings := Seq("play.server.http.port" -> "1234")
