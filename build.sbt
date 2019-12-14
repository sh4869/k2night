name := "k2night"
val fs2Version = "1.0.5"

lazy val fs2Dep = Seq(
  "co.fs2" %% "fs2-core",
  "co.fs2" %% "fs2-io",
  "co.fs2" %% "fs2-reactive-streams",
  "co.fs2" %% "fs2-experimental"
).map(_ % fs2Version)

lazy val macwireDep = Seq(
  "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % "2.3.3" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.3.3",
  "com.softwaremill.macwire" %% "proxy" % "2.3.3"
)


lazy val commonSettings = Seq(
  organization := "net.sh4869",
  scalaVersion := "2.12.6",
  version := "0.0.1",
  libraryDependencies ++= fs2Dep,
  libraryDependencies ++= macwireDep,
  libraryDependencies ++= Seq(
    "com.spinoco" %% "fs2-http" % "0.4.0",
    "org.typelevel" %% "cats-effect" % "2.0.0",
    "io.circe" %% "circe-fs2" % "0.11.0"
  )
)

commonSettings
