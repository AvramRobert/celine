package data

import com.ravram.nemesis.{Json, Read, Write}
import com.ravram.nemesis.coerce.Convert
import data.Channel._
import data.Source.{ChannelSearch, Storage, VideoSearch, Web}
import misc.Json.{ReadFrom, WriteTo}

import scala.jdk.CollectionConverters._

object Channel {
  opaque type ChannelName = String
  opaque type ChannelID = String

  given readCS: ReadFrom[ChannelSearch, Channel] =
    ReadFrom { json =>
      Json.read(json).using(
        js => js.transform().getValue(Read.STRING, "id"),
        js => js.transform().getValue(Read.STRING, "snippet", "title"),
        (id, title) => Channel(id, title)
      )
    }

  given readManyCS(using channel: ReadFrom[ChannelSearch, Channel]): ReadFrom[ChannelSearch, Vector[Channel]] =
    ReadFrom { json =>
      Json.read(json).using(
        js => js.transform().getValue(Read.list(channel.reads), "items"),
        list => list.asScala.toVector)
    }

  given readVS: ReadFrom[VideoSearch, Channel] =
    ReadFrom { json =>
      Json.read(json).using(
        js => js.transform().getValue(Read.STRING, "snippet", "channelId"),
        js => js.transform().getValue(Read.STRING, "snippet", "channelTitle"),
        (id, name) => Channel(id, name))
    }

  given readDB: ReadFrom[Storage, Channel] =
    ReadFrom { json =>
      Json.read(json).using(
        js => js.transform().getValue(Read.STRING, "id"),
        js => js.transform().getValue(Read.STRING, "name"),
        (id, name) => Channel(id, name))
    }

  given readManyDB(using channel: ReadFrom[Storage, Channel]): ReadFrom[Storage, Set[Channel]] =
    ReadFrom { json => Read.set(channel.reads).map(_.asScala.toSet).apply(json) }


  given writeDB: WriteTo[Storage, Channel] =
    WriteTo { channel =>
      Json.write(channel).using(
        "id", channel => Write.STRING.apply(channel.id),
        "name", channel => Write.STRING.apply(channel.name))
    }

  given writeManyDB(using channel: WriteTo[Storage, Channel]): WriteTo[Storage, Set[Channel]] =
    WriteTo { channels =>
      Write.set(channel.writes).apply(channels.asJava)
    }

  given writeWeb: WriteTo[Web, Channel] =
    WriteTo { channel =>
      Json.write(channel).using(
        "id", channel => Write.STRING.apply(channel.id),
        "name", channel => Write.STRING.apply(channel.name)
      )
    }

  given writeManyWeb(using channel: WriteTo[Web, Channel]): WriteTo[Web, Set[Channel]] =
    WriteTo { channels =>
      Write.set(channel.writes).apply(channels.asJava)
    }

  given readMarkersDB(using video: ReadFrom[Storage, Video]): ReadFrom[Storage, Map[ChannelID, Video]] =
    ReadFrom { json =>
      Read.map(video.reads).apply(json).map(_.asScala.toMap)
    }

  given writeMarkersDB(using video: WriteTo[Storage, Video]): WriteTo[Storage, Map[ChannelID, Video]] =
    WriteTo { map =>
      Write.map(video.writes).apply(map.asJava)
    }
}

final case class Channel(id: ChannelID, name: ChannelName)