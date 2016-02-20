name := "moco-scala"

organization := "org.treppo"

version := "0.3"

scalaVersion := "2.11.7"

scalacOptions in ThisBuild ++= {
  if (scalaVersion.value.startsWith("2.11"))
    Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")
  else
    Seq("-unchecked", "-deprecation", "-feature")
}

crossScalaVersions := Seq("2.10.6", "2.11.7")

libraryDependencies ++= Seq(
  "com.github.dreamhead" % "moco-core" % "0.10.2",
  "org.apache.httpcomponents" % "fluent-hc" % "4.2.5" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % Test
)