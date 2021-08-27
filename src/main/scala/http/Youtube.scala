package http

import com.ravram.nemesis.Json
import sttp.client3._

import scala.util.chaining._
import http.util.URL.Param._
import http.util.URL
import misc.Extensions._
import com.ravram.nemesis.Read
import data.{Channel, Video, Config}
import data.Channel.ChannelID
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
      .flatMap(json => json.transform().as(Video.readManySearch.reads).task)
  }

  def channel(id: String, config: Config)
             (using get: URL => Task[Json]): Task[Channel] = {
    channelURL
      .param(Key(config.apiKey))
      .param(Id(id))
      .pipe(get)
      .flatMap(json => json.transform().as(Channel.readManySearch.reads).task)
      .map(channels => channels.head)
  }
}