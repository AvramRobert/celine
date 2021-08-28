package data

import com.ravram.nemesis.{Json, Read, Write}
import data.Source.Storage
import data.Video
import misc.Json.{ReadFrom, WriteTo}

import java.time.ZonedDateTime
import java.util.UUID
import scala.jdk.CollectionConverters._

object Job {
  given readDB(using videos: ReadFrom[Storage, Vector[Video]]): ReadFrom[Storage, Job] =
    ReadFrom { json =>
      Json.read(json).using(
        js => js.transform().getValue(Read.UUID, "id"),
        js => js.transform().getValue(Read.ZONED_DATE_TIME, "created"),
        js => js.transform().getValue(videos.reads, "content"),
        (id, created, content) => Job(id, created, content))
    }

  given readManyDB(using job: ReadFrom[Storage, Job]): ReadFrom[Storage, Set[Job]] =
    ReadFrom { json =>
      Read.set(job.reads).apply(json).map(_.asScala.toSet)
    }

  given writeDB(using videos: WriteTo[Storage, Vector[Video]]): WriteTo[Storage, Job] =
    WriteTo { job =>
      Json.write(job).using(
        "id", j => Write.STRING.apply(j.id.toString),
        "created", j => Write.ZONED_DATE_TIME.apply(j.created),
        "content", j => videos.writes.apply(j.content))
    }

  given writeManyDB(using job: WriteTo[Storage, Job]): WriteTo[Storage, Set[Job]] =
    WriteTo { jobs =>
      Write.set(job.writes).apply(jobs.asJava)
    }
}

case class Job(id: UUID = UUID.randomUUID(),
               created: ZonedDateTime = ZonedDateTime.now(),
               content: Vector[Video])