package k2night

import java.nio.channels.AsynchronousChannelGroup
import cats.effect.ConcurrentEffect
import cats.effect.ContextShift
import cats.effect.Timer
import fs2.INothing
import fs2.Pipe
import fs2.Stream
import fs2.concurrent.Queue
import fs2.concurrent.Topic
import io.circe.Json
import io.circe.fs2.byteStreamParser
import io.circe.fs2.stringStreamParser
import spinoco.fs2.http
import spinoco.fs2.http.HttpRequest
import spinoco.fs2.http.websocket.Frame
import spinoco.fs2.http.websocket.WebSocketRequest
import spinoco.protocol.http._


class SlackResource[F[_] : ContextShift](token: String)(implicit F: ConcurrentEffect[F], AG: AsynchronousChannelGroup, timer: Timer[F]) {

  import SlackResource._

  implicit val codec = scodec.codecs.utf8

  private val topic = F.toIO(Topic[F, Json](Json.Null)).unsafeRunSync()

  private val queue = F.toIO(Queue.bounded[F, Json](100)).unsafeRunSync()

  def start: Stream[F, INothing] = Stream.eval(http.client[F]()).flatMap { client =>
    val request = HttpRequest.get[F](RTM_CONNECT_ENDPOINT.withQuery(Uri.Query("token", token)))
    client.request(request).flatMap { resp =>
      resp.body
    }.through(byteStreamParser).flatMap { v =>
      val url = Uri.parse(v.hcursor.downField("url").as[String].toOption.get).toOption.get
      def wsPipe: Pipe[F, Frame[String], Frame[String]] = { x =>
        val toTopic = x.map(_.a).through(stringStreamParser).through(topic.publish).drain
        val toQueue = queue.dequeue.map(v => Frame.Text(v.toString()))
        Stream(toTopic, toQueue).parJoinUnbounded
      }

      val request = WebSocketRequest.wss(url.host.host, url.path.stringify)
      client.websocket(request, wsPipe).drain
    }
  }

  def read: Stream[F, Json] = topic.subscribe(100)

  def write: Pipe[F, Json, Unit] = queue.enqueue

}

object SlackResource {
  val RTM_CONNECT_ENDPOINT = Uri.https("slack.com", "/api/rtm.connect")
}
