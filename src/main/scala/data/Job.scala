package data

import com.ravram.nemesis.{Json, Read, Write}
import data.Video
import misc.Json.{ReadFrom, WriteTo}

import java.time.ZonedDateTime
import java.util.UUID
import scala.jdk.CollectionConverters._

object Job {

  val readManyDatabase: ReadFrom[Source.Database, Set[Job]] = ReadFrom { json =>
    Read.list(readDatabase.reads).apply(json).map(_.asScala.toSet)
  }
  
  val writeManyDatabase: WriteTo[Source.Database, Set[Job]] = WriteTo { jobs =>
    Write.set(writeDatabase.writes).apply(jobs.asJava)
  }
  
  val readDatabase: ReadFrom[Source.Database, Job] = ReadFrom { json =>
    Json.read(json).using(
      js => js.transform().getValue(Read.UUID, "id"),
      js => js.transform().getValue(Read.ZONED_DATE_TIME, "created"),
      js => js.transform().getValue(Read.list(Video.readDatabase.reads), "content"),
      (id, created, content) => Job(id, created, content.asScala.toVector))
  }

  val writeDatabase: WriteTo[Source.Database, Job] = WriteTo { job =>
    Json.write(job).using(
      "id", j => Write.STRING.apply(j.id.toString),
      "created", j => Write.ZONED_DATE_TIME.apply(j.created),
      "content", j => Write.list(Video.writeDatabase.writes).apply(j.content.asJava))
  }
}

case class Job(id: UUID = UUID.randomUUID(),
               created: ZonedDateTime = ZonedDateTime.now(),
               content: Vector[Video])