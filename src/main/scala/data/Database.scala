package data

import com.ravram.nemesis.{Json, Read, Write}
import data.Channel.ChannelID
import data.{Channel, Job, Video}
import misc.Json.{ReadFrom, WriteTo}

import scala.jdk.CollectionConverters._

object Database {
  val empty: Database = Database(Set.empty, Map.empty, Set.empty)

  val read: ReadFrom[Source.Database, Database] = ReadFrom { json =>
    Json.read(json).using(
      js => js.transform().getValue(Channel.readManyDatabase.reads, "channels"),
      js => js.transform().getValue(Channel.readMarkers.reads, "markers"),
      js => js.transform().getValue(Job.readManyDatabase.reads, "jobs"),
      (channels, markers, jobs) => Database(channels.toSet, markers, jobs)
    )
  }

  val write: WriteTo[Source.Database, Database] = WriteTo { database =>
    Json.write(database).using(
      "channels", db => Channel.writeManyDatabase.writes(database.channels),
      "markers", db => Channel.writeMarkers.writes.apply(database.markers),
      "jobs",    db => Job.writeManyDatabase.writes.apply(database.jobs))
  }
}

final case class Database(channels: Set[Channel], markers: Map[ChannelID, Video], jobs: Set[Job])