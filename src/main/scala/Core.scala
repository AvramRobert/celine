import data.{Channel, Config, Database, Downloads, Job, Video}
import http.util.{Http, URL}
import http.Youtube
import shell.Programs
import shell.util.Command.{ShellCommand, Termination}
import storage.LocalFile
import java.io.File

import scala.util.chaining._
import misc.Extensions._
import zio._
import com.ravram.nemesis.Json

object Core {

  val maxResults = 50

  type OTask[A] = ZIO[clock.Clock, Throwable, A]

  def succeed[A](a: => A): OTask[A] = ZIO.succeed(a)

  def gather(channel: Channel, from: Video, config: Config)
            (using get: URL => Task[Json]): Task[Vector[Video]] = {
    Youtube.videos(
      channel = channel,
      config = config,
      maxResults = maxResults,
      after = Some(from.publishedAt)).flatMap { videos =>
      if (videos.isEmpty)
      then Task.succeed(videos)
      else if (videos.head.id == from.id)
      then Task.succeed(videos)
      else gather(channel, videos.head, config).map(rest => videos ++ rest)
    }
  }

  def track(videos: Vector[Vector[Video]]): Database => Database = database =>
    videos.foldLeft(database) {
      case (db, video +: _) =>
        db.copy(markers = db.markers.updated(video.channel.id, video))
      case (db, _) => db
    }

  def enqueue(playlistJob: Job): Database => Database = database =>
    database.copy(jobs = database.jobs + playlistJob)

  def createPlaylist(job: Job, config: Config)
                    (using
                     sh: ShellCommand => Task[Termination],
                     log: String => Task[Unit]): OTask[Downloads] =
    for {
      _          <- log("Preparing playlist directory")
      jobDate    <- Task(job.created.toLocalDate)
      folder      = File(s"${config.outputDirectory.toPath.toAbsolutePath}/orchestra_${jobDate.toString}/")
      _          <- Programs.createDir(folder)
      _          <- log("Downloading playlist files")
      successful <- Task.collectAllSuccessesPar {
        job.content.map { video => Programs.retrieveAudio(video, folder) }
      }
      failed     = job.content.filterNot(successful.contains)
      _          <- if (failed.size > 0)
      then log(s"Failed to download: ${failed.size}. Will be retried later")
      else succeed(())
    } yield Downloads(succeeded = successful, failed = failed)

  def processJobs(db: Database, config: Config)
                 (using
                  sh: (ShellCommand => Task[Termination]),
                  spit: (String, File) => Task[Unit],
                  log: (String => Task[Unit])): OTask[Database] = db.jobs.foldLeft(succeed(db)) {
    (dbM, job) =>
      for {
        db     <- dbM
        result <- createPlaylist(job, config)
        newDB  = if (result.failed.nonEmpty)
        then db.copy(jobs = db.jobs - job + Job(content = result.failed))
        else db.copy(jobs = db.jobs - job)
        _     <- LocalFile.commit(newDB, config.databaseFile)
      } yield newDB
  }

  def playlist(config: Config)
              (using
               sh: ShellCommand => Task[Termination],
               slurp: File => Task[Option[String]],
               spit: (String, File) => Task[Unit],
               log: String => Task[Unit]): OTask[Unit] =
    for {
      _       <- log("Playlist Job initiated")
      db      <- LocalFile.load(config.databaseFile)
      updated <- processJobs(db, config)
      _       <- LocalFile.commit(updated, config.databaseFile)
      _       <- log("Jobs completed")
    } yield ()

  def newVideos(database: Database, config: Config)
               (using get: URL => Task[Json]): Task[Vector[Vector[Video]]] = Task.collectAll {
    database.channels.toVector.map { channel =>
      database.markers.get(channel.id) match {
        case Some(video) => gather(channel, video, config)
        case None => Youtube.videos(channel = channel, config = config, maxResults = maxResults)
      }
    }
  }

  def process(using
              config: Task[Config],
              sh: ShellCommand => Task[Termination],
              slurp: File => Task[Option[String]],
              spit: (String, File) => Task[Unit],
              get: URL => Task[Json],
              log: String => Task[Unit]): OTask[Unit] =
    for {
      _      <- log("Starting process")
      conf   <- config
      db     <- LocalFile.load(conf.databaseFile)
      videos <- newVideos(db, conf)
      _      <- log("Gathered all videos")
      job     = Job(content = videos.flatten.toVector)
      updated = db.pipe(track(videos)).pipe(enqueue(job))
      _      <- LocalFile.commit(updated, conf.databaseFile)
      _      <- log(s"Database updated")
      _      <- log("Scheduling playlist creation job")
      _      <- playlist(conf)
    } yield ()

  def store(channelId: String)
           (using
            config: Task[Config],
            slurp: File => Task[Option[String]],
            spit: (String, File) => Task[Unit],
            get: URL => Task[Json],
            log: String => Task[Unit]): OTask[Unit] = {
    for {
      _       <- log(s"Trying to store channel: $channelId")
      conf    <- config
      channel <- Youtube.channel(channelId, conf)
      _       <- log(s"Found channel: $channel")
      db      <- LocalFile.load(conf.databaseFile)
      updated = db.copy(channels = db.channels + channel)
      _       <- LocalFile.commit(updated, conf.databaseFile)
      _       <- log("Channel stored")
    } yield ()
  }

  def channels(using
               config: Task[Config],
               slurp: File => Task[Option[String]]): OTask[Json] = {
    for {
      conf     <- config
      channels <- LocalFile.read(conf.databaseFile)(_.channels)
      json     <- Channel.writeManyWeb.writes.apply(channels.toVector).task
    } yield json
  }
}
