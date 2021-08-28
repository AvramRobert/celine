package data

import com.ravram.nemesis.{Json, Read, Write}
import data.Video.VideoID
import data.Channel
import data.Source.{Storage, VideoSearch}
import http.Youtube

import java.time.ZonedDateTime
import http.util.URL.Param
import http.util.URL
import misc.Json.{ReadFrom, WriteTo}

import scala.jdk.CollectionConverters._

object Video {
  opaque type VideoID = String

  given readVS(using channel: ReadFrom[VideoSearch, Channel]): ReadFrom[VideoSearch, Video] =
    ReadFrom { json =>
      val jsonT = json.transform()
      for {
        id          <- jsonT.getValue(Read.STRING, "id", "videoId")
        title       <- jsonT.getValue(Read.STRING, "snippet", "title")
        url         = Youtube.watchURL.param(Param.V(id)).encode
        publishedAt <- jsonT.getValue(Read.ZONED_DATE_TIME, "snippet", "publishedAt")
        channel     <- jsonT.as(channel.reads)
      } yield Video(id, title, URL(url), publishedAt, channel)
    }

  given readManyVS(using video: ReadFrom[VideoSearch, Video]): ReadFrom[VideoSearch, Vector[Video]] =
    ReadFrom { json =>
      Json.read(json).using(
        js   => js.transform().getValue(Read.list(video.reads), "items"),
        list => list.asScala.toVector)
    }
  
  given readDB(using channel: ReadFrom[Storage, Channel]): ReadFrom[Storage, Video] = 
    ReadFrom { json =>
      val jsonT = json.transform()
      for {
        id          <- jsonT.getValue(Read.STRING, "id")
        title       <- jsonT.getValue(Read.STRING, "title")
        url         <- jsonT.getValue(Read.STRING, "url")
        publishedAt <- jsonT.getValue(Read.ZONED_DATE_TIME, "publishedAt")
        channel     <- jsonT.getValue(channel.reads, "channel")
      } yield Video(id, title, URL(url), publishedAt, channel)
    }

  given readManyDB(using video: ReadFrom[Storage, Video]): ReadFrom[Storage, Vector[Video]] =
    ReadFrom { json =>
      Read.list(video.reads).map(_.asScala.toVector).apply(json)
    }
  
  given writeDB(using channel: WriteTo[Storage, Channel]): WriteTo[Storage, Video] =
    WriteTo { video =>
      Json.write(video).using(
        "id",          video => Write.STRING.apply(video.id),
        "title",       video => Write.STRING.apply(video.title),
        "url",         video => Write.STRING.apply(video.url.encode),
        "publishedAt", video => Write.ZONED_DATE_TIME.apply(video.publishedAt),
        "channel",     video => channel.writes.apply(video.channel))
    }

  given writeManyDB(using video: WriteTo[Storage, Video]): WriteTo[Storage, Vector[Video]] =
    WriteTo { videos =>
      Write.list(video.writes).apply(videos.asJava)
    }
}

final case class Video(id: VideoID, title: String, url: URL, publishedAt: ZonedDateTime, channel: Channel)