package data

import com.ravram.nemesis.{Attempt, Json, Read, Write}
import com.ravram.nemesis.coerce.Convert
import data.Channel._
import data.Video.readSearch
import misc.Json.{ReadFrom, WriteTo}

import scala.jdk.CollectionConverters._

object Channel {
  opaque type ChannelName = String
  opaque type ChannelID = String

  val readManySearch: ReadFrom[Source.ChannelSearch, Vector[Channel]] = ReadFrom { json =>
    Json.read(json).using(
      js => js.transform().getValue(Read.list(readChannelSearch.reads), "items"),
      list => list.asScala.toVector)
  }

  val readChannelSearch: ReadFrom[Source.ChannelSearch, Channel] = ReadFrom { json =>
    Json.read(json).using(
      js => js.transform().getValue(Read.STRING, "id"),
      js => js.transform().getValue(Read.STRING, "snippet", "title"),
      (id, title) => Channel(id, title)
    )
  }

  val readVideoSearch: ReadFrom[Source.VideoSearch, Channel] = ReadFrom { json =>
    Json.read(json).using(
      js => js.transform().getValue(Read.STRING, "snippet", "channelId"),
      js => js.transform().getValue(Read.STRING, "snippet", "channelTitle"),
      (id, name) => Channel(id, name))
  }

  val readManyDatabase: ReadFrom[Source.Database, Vector[Channel]] = ReadFrom { json =>
    Read.list(readDatabase.reads).map(list => list.asScala.toVector).apply(json)
  }
  
  val writeManyWeb: WriteTo[Source.Web, Vector[Channel]] = WriteTo { vector =>
    Write.list(writeWeb.writes).apply(vector.asJava)
  }
  
  val writeWeb: WriteTo[Source.Web, Channel] = WriteTo { channel =>
    Json.write(channel).using(
      "id", channel => Write.STRING.apply(channel.id),
      "name", channel => Write.STRING.apply(channel.name)
    )
  }

  val readDatabase: ReadFrom[Source.Database, Channel] = ReadFrom { json =>
    Json.read(json).using(
      js => js.transform().getValue(Read.STRING, "id"),
      js => js.transform().getValue(Read.STRING, "name"),
      (id, name) => Channel(id, name))
  }

  val writeManyDatabase: WriteTo[Source.Database, Set[Channel]] = WriteTo { channels =>
    Write.set(writeDatabase.writes).apply(channels.asJava)
  }

  val writeDatabase: WriteTo[Source.Database, Channel] = WriteTo { channel =>
    Json.write(channel).using(
      "id", channel => Write.STRING.apply(channel.id),
      "name", channel => Write.STRING.apply(channel.name))
  }

  val readMarkers: ReadFrom[Source.Database, Map[ChannelID, Video]] = ReadFrom { json =>
    Read.map(Video.readDatabase.reads).apply(json).map(_.asScala.toMap)
  }

  val writeMarkers: WriteTo[Source.Database, Map[ChannelID, Video]] = WriteTo { map =>
    Write.map(Video.writeDatabase.writes).apply(map.asJava)
  }
}

final case class Channel(id: ChannelID, name: ChannelName)