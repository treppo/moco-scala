name := "moco-scala"

organization := "org.treppo"

version := "0.5.0"

scalaVersion := "2.11.7"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

crossScalaVersions := Seq("2.10.6", "2.11.7")

libraryDependencies ++= Seq(
  "com.github.dreamhead" % "moco-core" % "0.10.2",
  "org.apache.httpcomponents" % "fluent-hc" % "4.2.5" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % Test
)