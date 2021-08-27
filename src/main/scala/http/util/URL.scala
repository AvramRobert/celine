package http.util

import data.Channel.ChannelID
import data.Video.VideoID
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import http.util.URL.Param

object URL {
  enum Param[A]:
    case Type(value: String) extends Param[String]
    case Part(value: String) extends Param[String]
    case Order(value: String) extends Param[String]
    case MaxResults(value: Int) extends Param[Int]
    case Id(value: String) extends Param[String]
    case Mine(value: Boolean) extends Param[Boolean]
    case ChannelId(value: ChannelID) extends Param[String]
    case PublishedAfter(value: ZonedDateTime) extends Param[ZonedDateTime]
    case Key(value: String) extends Param[String]
    case V(value: VideoID) extends Param[VideoID]

  def encode(url: URL): String = url.encode

  def encode[A](param: Param[A]): String = param match {
    case Param.Type(value) => s"type=$value"
    case Param.Part(value) => s"part=$value"
    case Param.Order(value) => s"order=$value"
    case Param.Mine(value) => s"mine=$value"
    case Param.MaxResults(value) => s"maxResults=$value"
    case Param.ChannelId(value) => s"channelId=$value"
    case Param.Id(value) => s"id=$value"
    case Param.Key(value) => s"key=$value"
    case Param.PublishedAfter(value) => s"publishedAfter=${value.format(DateTimeFormatter.ISO_DATE_TIME)}"
    case Param.V(value) => s"v=$value"
  }
}

final case class URL(urlBase: String, private val params: Vector[String] = Vector.empty) {
  def encode: String = if (params.isEmpty) then urlBase else s"$urlBase?${params.mkString("&")}"
  def path(path: String): URL = URL(urlBase + path, params)
  def param[A](param: Param[A]): URL = URL(urlBase, params :+ URL.encode(param))
  def param[A](opt: Option[Param[A]]): URL = opt.fold(this)(param)
}

