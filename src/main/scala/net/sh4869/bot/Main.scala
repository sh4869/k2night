package net.sh4869.bot

import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors
import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.functor._
import fs2.Pipe
import fs2.Stream
import io.circe.Json
import k2night.Core.Bot
import k2night.Core.Input
import k2night.Core.Process
import k2night.SlackResource
import k2night.StdInOut

object MainApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    Stream.resource(Blocker[IO]).flatMap(v => {
      implicit val ag: AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool())
      val stdInOut = new StdInOut[IO](v)
      val input: Input[IO, String] = stdInOut.stdin
      val pipe: Process[IO, String, String] = _.filter(!_.isEmpty).map(v => s"> ${v.toDouble + 1}\n")
      val output: Pipe[IO, String, Unit] = stdInOut.stdout
      val bot = Bot("bot1", input, pipe, output)

      val token = ""
      val slackResource = new SlackResource[IO](token)
      val input2 = slackResource.read
      val pipe2: Process[IO, Json, Json] = _.filter(_.hcursor.downField("type").as[String].exists(_ == "message")).map(v => {
        println(v)
        val json = for {
          channel <- v.hcursor.downField("channel").as[String]
          text <- v.hcursor.downField("text").as[String]
        } yield Json.obj(
          ("channel", Json.fromString(channel)),
          ("text", Json.fromString(text)),
          ("type", Json.fromString("message")),
          ("id", Json.fromInt(1))
        )
        println(json)
        json
      }).filter(_.isRight).map(_.right.get)
      val output2: Pipe[IO, Json, Unit] = slackResource.write

      val bot2 = Bot("bot2", input2, pipe2, output2)

      Stream(bot.stream(stdInOut.stdout), bot2.stream(stdInOut.stdout), stdInOut.start, slackResource.start).parJoinUnbounded
    }).compile.drain.as(ExitCode.Success)
  }
}



