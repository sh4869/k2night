package k2night.io

import cats.effect.unsafe.implicits.global

import cats.effect.std.Queue
import cats.effect.Async
import fs2.Stream
import fs2.Pipe
import fs2.concurrent.Topic
import fs2.io
import fs2.text
import cats.implicits._
import cats.effect._
import cats.effect.kernel.syntax.resource
import k2night.StreamResourceManager

class StdIOResource[F[_]](implicit A: Async[F])
    extends StreamResourceManager[F, String, String] {

  def startStreams(
      topic: Topic[F, String],
      queue: Queue[F, String]
  ): Stream[F, Unit] = {
    Stream(
      io
        .stdinUtf8(100)
        .through(text.lines)
        .through(topic.publish),
      Stream
        .repeatEval(queue.take)
        .through(io.stdoutLines())
    ).parJoinUnbounded
  }

  def release = A.unit
}
