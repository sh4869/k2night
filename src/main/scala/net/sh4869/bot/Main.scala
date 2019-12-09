package net.sh4869.bot

import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.functor._
import fs2.Pipe
import fs2.Stream
import net.sh4869.bot.Core._

object MainApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    Stream.resource(Blocker[IO]).flatMap(v => {
      val stdInOut = new StdInOut[IO](v)
      val input: Input[IO, String] = stdInOut.stdin
      val pipe: Process[IO, String, String] = _.filter(!_.isEmpty).map(v => s"> ${v.toDouble + 1}\n")
      val output: Pipe[IO, String, Unit] = stdInOut.stdout
      val bot = Bot("bot1", input, pipe, output)
      Stream(bot.stream(stdInOut.stdout), stdInOut.start).parJoinUnbounded
    }).compile.drain.as(ExitCode.Success)
  }
}



