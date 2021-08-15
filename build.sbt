name := "k2night"
val fs2Version = "3.1.0"

lazy val fs2Dep = Seq(
  "co.fs2" %% "fs2-core",
  "co.fs2" %% "fs2-io"
).map(_ % fs2Version)

lazy val commonSettings = Seq(
  organization := "net.sh4869",
  scalaVersion := "3.0.1",
  version := "0.0.1",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect-kernel" % "3.2.2",
    "org.typelevel" %% "cats-effect-std" % "3.2.2",
    "org.typelevel" %% "cats-effect" % "3.2.2",
    "org.typelevel" %% "cats-core" % "2.6.1",
    "io.circe" %% "circe-fs2" % "0.14.0"
  ) ++ fs2Dep
)

lazy val root = project.in(file(".")).settings(commonSettings)
