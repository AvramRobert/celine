package data

import data.Channel.ChannelID
import data.Source.Storage
import data.{Channel, Job, Video}
import misc.Json.{ReadFrom, WriteTo, read, write}

import com.ravram.nemesis.Json
import scala.jdk.CollectionConverters._

object Database {
  val empty: Database = Database(Set.empty, Map.empty, Set.empty)

  given readDB: ReadFrom[Storage, Database] =
    ReadFrom { json =>
      Json.read(json).using(
        js => js.transform().getValue(read[Storage, Set[Channel]], "channels"),
        js => js.transform().getValue(read[Storage, Map[ChannelID, Video]], "markers"),
        js => js.transform().getValue(read[Storage, Set[Job]], "jobs"),
        (channels, markers, jobs) => Database(channels, markers, jobs)
      )
    }

  given writeDB: WriteTo[Storage, Database] =
    WriteTo { database =>
      Json.write(database).using(
        "channels", db => write[Storage, Set[Channel]].apply(database.channels),
        "markers", db => write[Storage, Map[ChannelID, Video]].apply(database.markers),
        "jobs",    db => write[Storage, Set[Job]].apply(database.jobs))
    }

}

final case class Database(channels: Set[Channel], markers: Map[ChannelID, Video], jobs: Set[Job])