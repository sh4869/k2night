name := "botFramework"

version := "0.1"

scalaVersion := "2.13.1"


// available for Scala 2.11, 2.12, 2.13
libraryDependencies += "co.fs2" %% "fs2-core" % "2.1.0" // For cats 2 and cats-effect 2

// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % "2.1.0"

// optional reactive streams interop
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % "2.1.0"

// optional experimental library
libraryDependencies += "co.fs2" %% "fs2-experimental" % "2.1.0"

libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"

libraryDependencies += "com.softwaremill.macwire" %% "macrosakka" % "2.3.3" % "provided"

libraryDependencies += "com.softwaremill.macwire" %% "util" % "2.3.3"

libraryDependencies += "com.softwaremill.macwire" %% "proxy" % "2.3.3"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0"
