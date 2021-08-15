package net.sh4869.bot

import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.functor._
import cats.effect.implicits._
import fs2.Pipe
import fs2.Stream
import fs2.io
import fs2.text
import k2night._
import k2night.io.StdIOResource
import fs2.concurrent.Topic
import cats.effect.std.Queue

object MainApp extends IOApp {
  def run(args: List[String]) = {

    new StdIOResource[IO]().resource
      .use { r =>
        {
          val z = r._2.input.map(v => "line:(" + v + ") ").through(r._2.output)
          val x =
            r._2.input.map(v => "line(2):(" + v + ") ").through(r._2.output)
          Stream(
            z,
            x,
            r._1
          ).parJoinUnbounded.compile.drain
        }
      }
      .as(ExitCode.Success)
  }
}
