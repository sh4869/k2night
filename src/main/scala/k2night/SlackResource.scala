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
    // TODO: Create Case class of response and Option -> Either
    client.request(rtmConnectRequest[F](token)).flatMap(_.body).through(byteStreamParser).map { v =>
      for {
        rawUrl <- v.hcursor.downField("url").as[String].toOption
        url <- Uri.parse(rawUrl).toOption
      } yield WebSocketRequest.wss(url.host.host, url.path.stringify)
    }.flatMap {
      _.map(client.websocket(_, wsPipe).drain).getOrElse(Stream.raiseError(new Exception("can not connect to rtm server")))
    }
  }

  def read: Stream[F, Json] = topic.subscribe(100)

  def write: Pipe[F, Json, Unit] = queue.enqueue

  def wsPipe: Pipe[F, Frame[String], Frame[String]] = { x =>
    val toTopic = x.map(_.a).through(stringStreamParser).through(topic.publish).drain
    val toQueue = queue.dequeue.map(v => Frame.Text(v.toString()))
    Stream(toTopic, toQueue).parJoinUnbounded
  }
}

object SlackResource {
  val RTM_CONNECT_ENDPOINT: Uri = Uri.https("slack.com", "/api/rtm.connect")

  def rtmConnectRequest[F[_]]: String => HttpRequest[F] = token => HttpRequest.get[F](RTM_CONNECT_ENDPOINT.withQuery(Uri.Query("token", token)))
}
