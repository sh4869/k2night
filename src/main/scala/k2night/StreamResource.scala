package k2night

import fs2.Stream
import fs2.concurrent.Topic
import cats.effect.std.Queue
import cats.effect.kernel.Resource
import cats.effect.kernel.Async
import cats.effect.syntax._
import cats.implicits._


trait StreamResource[F[_], I, O] {
  def input: Input[F, I]
  def output: Output[F, O]
}

/**
 * BotのStreamリソースを扱うためのクラス
 */
trait StreamResourceManager[F[_] : Async, I, O] {
  /**
   * リソースを使うためのStreamを返す関数
   */ 
  protected def startStreams(topic: Topic[F, I], queue: Queue[F, O]): Stream[F, Unit]
  /**
   * リソースを解放するための関数
   */
  protected def release: F[Unit]
  /**
   * リソースを取得するための関数
   */
  def resource: Resource[F, (Stream[F, Unit], StreamResource[F, I, O])] = {
    val a = for {
      topic <-Topic[F, I]
      queue <- Queue.unbounded[F, O]
    } yield (startStreams(topic, queue), new EventPubSubManager(topic, queue))
    val r = (v: (Stream[F, Unit], EventPubSubManager[F, I, O])) => {
      for {
        _ <- release
        _ <- v._2.release
      } yield ()
    }
    Resource.make(a)(r)
  }
}

private case class EventPubSubManager[F[_]: Async, I, O](
    val topic: Topic[F, I],
    val queue: Queue[F, O]
) extends StreamResource[F, I, O] {
  def input: Input[F, I] = topic.subscribe(100)
  def output: Output[F, O] = i => i.evalMap(v => queue.offer(v))
  def release = topic.close.map(_ => ())
}
