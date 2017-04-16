package com.github.okapies.akka.mastodon.scaladsl

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.{Failure, Success, Try}
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.github.okapies.akka.mastodon.Status
import com.github.okapies.akka.mastodon.Timeline
import io.circe.{Decoder, Json, ParsingFailure}

final class MastodonClient(val endpoint: Uri, val accessToken: String)
                          (implicit system: ActorSystem, materializer: ActorMaterializer) {

  import io.circe.parser._
  import MastodonClient._

  private[this] def authzHeader = RawHeader(name = "Authorization", value = s"Bearer $accessToken")

  private[this] val poolClientFlow
      : Flow[(HttpRequest, NotUsed), (Try[HttpResponse], NotUsed), Http.HostConnectionPool] = {
    val host = endpoint.authority.host.address
    val port = endpoint.effectivePort

    endpoint.scheme match {
      case "http" => Http().cachedHostConnectionPool[NotUsed](host, port)
      case "https" => Http().cachedHostConnectionPoolHttps[NotUsed](host, port)
    }
  }

  def timelines(timeline: Timeline): Source[Status, NotUsed] = {
    val req = HttpRequest(uri = timeline.path, headers = List(authzHeader)) -> NotUsed

    Source.single(req).via(poolClientFlow).flatMapConcat(toContent)
      .flatMapConcat(content => toSource(parse(content).flatMap(asTimeline)))
  }

}

object MastodonClient {

  def apply(
      endpoint: String,
      accessToken: String)
      (implicit system: ActorSystem, materializer: ActorMaterializer) =
    new MastodonClient(endpoint, accessToken)

  import io.circe.generic.auto._

  private[this] val dateFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  private[this] implicit val decodeDate: Decoder[LocalDateTime] =
    Decoder[String].emapTry { s =>
      try {
        Success(LocalDateTime.parse(s, dateFormater))
      } catch {
        case t: Throwable => Failure(t)
      }
    }

  private[this] implicit val decodeUri: Decoder[Uri] = Decoder[String].map(p => Uri(p))

  private[MastodonClient] def toContent(response: (Try[HttpResponse], _)): Source[String, _] = response._1 match {
    case Success(r) => r.entity.dataBytes.map(_.decodeString(ByteString.UTF_8)).fold("")(_ + _)
    case Failure(e) => Source.failed(e)
  }

  private[MastodonClient] def toSource[A](a: Either[ParsingFailure, Source[A, _]]): Source[A, _] = a match {
    case Right(as) => as
    case Left(e) => Source.failed(e)
  }

  private[MastodonClient] def asTimeline(json: Json): Either[ParsingFailure, Source[Status, _]] = {
    json.asArray match {
      case Some(ss) => Right(Source(ss).flatMapConcat(asStatus))
      case None => Left(new ParsingFailure("Root node is expected to be an array.", null))
    }
  }

  private[MastodonClient] def asStatus(json: Json): Source[Status, _] = {
    json.as[Status] match {
      case Right(s) => Source.single(s)
      case Left(e) => Source.failed(e)
    }
  }

}
