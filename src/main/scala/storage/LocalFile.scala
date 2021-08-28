package storage

import data.Source.Storage
import data.Database
import misc.Extensions._
import misc.Json.{read, write}

import java.io.File
import java.nio.file.Files
import com.ravram.nemesis.Json
import scala.util.chaining._
import zio.Task

object LocalFile {

  def load(file: File)
          (using slurp: File => Task[Option[String]]): Task[Database] =
    slurp(file).flatMap {
      case Some(text) => Json.parseAs(read[Storage, Database], text).task
      case None => Task.succeed(Database.empty)
    }
  
  def commit(database: Database, to: File)
            (using spit: (String, File) => Task[Unit]): Task[Unit] = {
    write[Storage, Database]
      .apply(database)
      .map(_.encode())
      .task
      .flatMap(json => spit(json, to))
  }
}
