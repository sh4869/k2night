package k2night

import cats.effect.Blocker
import cats.effect.ConcurrentEffect
import cats.effect.ContextShift
import fs2.Stream
import fs2.concurrent.Queue
import fs2.concurrent.Topic
import fs2.io
import fs2.text
import k2night.Core._

class StdInOut[F[_] : ContextShift](blocker: Blocker)(implicit F: ConcurrentEffect[F]) {

  private val topic = F.toIO(Topic[F, String]("")).unsafeRunSync()

  private val queue = F.toIO(Queue.bounded[F, String](100)).unsafeRunSync()

  private val inputS = io.stdin[F](4096, blocker.blockingContext).through(text.utf8Decode).through(topic.publish)

  private val outputS = queue.dequeue.through(text.utf8Encode).through(io.stdout[F](blocker.blockingContext))

  def start: Stream[F, Unit] = Stream(inputS, outputS).parJoinUnbounded

  def stdin: Input[F, String] = topic.subscribe(100)

  def stdout: Output[F, String] = queue.enqueue
}
