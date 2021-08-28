package http

import com.ravram.nemesis.Json
import sttp.client3._

import scala.util.chaining._
import http.util.URL.Param._
import http.util.URL
import misc.Extensions._
import com.ravram.nemesis.Read
import data.{Channel, Config, Video}
import data.Channel.ChannelID
import data.Source.{ChannelSearch, VideoSearch}
import misc.Json.{ReadFrom, read}
import zio.Task

import java.time.ZonedDateTime
import scala.jdk.CollectionConverters._

object Youtube {

  val apiURL = URL("https://www.googleapis.com/youtube/v3")

  val videoSearchURL =
    apiURL
      .path("/search")
      .param(Type("video"))
      .param(Part("snippet"))
      .param(Order("date"))

  val watchURL = URL("https://www.youtube.com/watch")

  val channelURL =
      apiURL
      .path("/channels")
      .param(Part("contentDetails,snippet"))
      .param(MaxResults(1))

  def videos(channel: Channel, config: Config, maxResults: Int = 10, after: Option[ZonedDateTime] = None)
            (using get: URL => Task[Json]): Task[Vector[Video]] = {

    videoSearchURL
      .param(Key(config.apiKey))
      .param(ChannelId(channel.id))
      .param(MaxResults(maxResults))
      .param(after.map(after => PublishedAfter(after)))
      .pipe(get)
      .map(_.as(read[VideoSearch, Vector[Video]]))
      .flatMap(_.task)
  }

  def channel(id: String, config: Config)
             (using get: URL => Task[Json]): Task[Channel] = {
    channelURL
      .param(Key(config.apiKey))
      .param(Id(id))
      .pipe(get)
      .map(_.as(read[ChannelSearch, Vector[Channel]]))
      .flatMap(_.task)
      .map(channels => channels.head)
  }
}