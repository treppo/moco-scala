sonatypeSettings

name := "moco-scala"

organization := "org.treppo"

version := "0.3"

scalaVersion := "2.11.7"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

crossScalaVersions := Seq("2.10.6", "2.11.7")

libraryDependencies ++= Seq(
  "com.github.dreamhead" % "moco-core" % "0.10.2",
  "org.apache.httpcomponents" % "fluent-hc" % "4.2.5" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % Test
)

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra :=
  <url>https://github.com/treppo/moco-scala</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>https://raw.github.com/treppo/moco-scala/master/MIT-LICENSE.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:treppo/moco-scala.git</url>
    <connection>scm:git:git@github.com:treppo/moco-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>treppo</id>
      <name>Christian Treppo</name>
      <url>https://treppo.org</url>
    </developer>
  </developers>
