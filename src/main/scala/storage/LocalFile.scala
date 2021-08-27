package storage

import com.ravram.nemesis.Json
import data.{Database, Video}
import storage.util.FileSystem.{slurp, spit}
import misc.Extensions._
import java.io.File
import java.nio.file.Files
import scala.util.chaining._
import zio.Task

object LocalFile {
 
  def read[A](file: File)
             (f: Database => A)
             (using slurp: File => Task[Option[String]]): Task[A] =
    load(file).map(f)
  
  def load(file: File)
          (using slurp: File => Task[Option[String]]): Task[Database] =
    slurp(file).flatMap {
      case Some(text) => Json.parseAs(Database.read.reads, text).task
      case None => Task.succeed(Database.empty)
    }
  
  def commit(database: Database, to: File)
            (using spit: (String, File) => Task[Unit]): Task[Unit] =
    Database
      .write
      .writes
      .apply(database)
      .map(_.encode())
      .task
      .flatMap(json => spit(json, to))
}
