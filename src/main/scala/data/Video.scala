package data

import com.ravram.nemesis.{Json, Read, Write}
import data.Video.VideoID
import data.Channel
import http.Youtube

import java.time.ZonedDateTime
import http.util.URL.Param
import http.util.URL
import misc.Json.{ReadFrom, WriteTo}
import scala.jdk.CollectionConverters._

object Video {
  opaque type VideoID = String

  val readManySearch: ReadFrom[Source.VideoSearch, Vector[Video]] = ReadFrom { json =>
      Json.read(json).using(
        js => js.transform().getValue(Read.list(readSearch.reads), "items"),
        list => list.asScala.toVector)
  }

  val readSearch: ReadFrom[Source.VideoSearch, Video] = ReadFrom { json =>
    val jsonT = json.transform()
    for {
      id <- jsonT.getValue(Read.STRING, "id", "videoId")
      title <- jsonT.getValue(Read.STRING, "snippet", "title")
      url = Youtube.watchURL.param(Param.V(id)).encode
      publishedAt <- jsonT.getValue(Read.ZONED_DATE_TIME, "snippet", "publishedAt")
      channel <- jsonT.as(Channel.readVideoSearch.reads)
    } yield Video(id, title, URL(url), publishedAt, channel)
  }

  val readDatabase: ReadFrom[Source.Database, Video] = ReadFrom { json =>
    val jsonT = json.transform()
    for {
      id <- jsonT.getValue(Read.STRING, "id")
      title <- jsonT.getValue(Read.STRING, "title")
      url <- jsonT.getValue(Read.STRING, "url")
      publishedAt <- jsonT.getValue(Read.ZONED_DATE_TIME, "publishedAt")
      channel <- jsonT.getValue(Channel.readDatabase.reads, "channel")
    } yield Video(id, title, URL(url), publishedAt, channel)
  }

  val writeDatabase: WriteTo[Source.Database, Video] = WriteTo { video =>
    Json.write(video).using(
      "id", video => Write.STRING.apply(video.id),
      "title", video => Write.STRING.apply(video.title),
      "url", video => Write.STRING.apply(video.url.encode),
      "publishedAt", video => Write.ZONED_DATE_TIME.apply(video.publishedAt),
      "channel", video => Channel.writeDatabase.writes.apply(video.channel))
  }
}

final case class Video(id: VideoID, title: String, url: URL, publishedAt: ZonedDateTime, channel: Channel)