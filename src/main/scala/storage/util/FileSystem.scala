package storage.util

import java.io.File
import java.nio.file.{Files, Path, Paths}
import zio.Task

object FileSystem {
  private def path(file: File): Path = Paths.get(file.toPath.toAbsolutePath.toUri)

  def slurp(file: File): Task[Option[String]] = Task.effect {
    val p = path(file)
    if (Files.exists(p))
    then Some(Files.readString(path(file)))
    else None
  }

  def spit(content: String, file: File): Task[Unit] = Task.effect {
    Files.write(path(file), content.getBytes())
    ()
  }
}
