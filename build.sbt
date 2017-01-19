import Dependencies._

name := """scrubber-play-twitter4j"""

organization := "44Lab5"

version := "2.0"

scalaVersion := "2.11.7"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, UniversalPlugin, DockerPlugin, PlayAkkaHttpServer)
  .disablePlugins(PlayNettyServer)
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
        aspectJ,
        kamonCore,
        kamonScala,
        kamonAkka,
        kamonAnnotation,
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

//Settings for docker publish plugin
dockerExposedPorts := Seq(1234, 1234)


resolvers ++= Dependencies.resolutionRepos

aspectjSettings

parallelExecution in run := true
fork in run := false

javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

//This is where we set the port that we will use for the application
PlayKeys.devSettings := Seq("play.server.http.port" -> "1234")
