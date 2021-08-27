package shell

import java.io.File
import data.Video
import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.text.StringEscapeUtils.unescapeHtml4
import shell.util.Command._
import zio._

import java.nio.file.{Files, Paths}
import java.util.UUID

object Programs {

  private def file(dir: File, fileName: String, ext: String): File =
    File(s"${dir.toPath.toAbsolutePath}/$fileName.$ext")

  def createDir(destDir: File)
               (using ShellCommand => Task[Termination]): Task[Unit] =
    for {
      exists <- Task(destDir.exists())
      _      <- Task {
        if (exists)
        then ()
        else Files.createDirectory(Paths.get(destDir.toURI))
      }
    } yield ()
  
  def retrieveAudio(video: Video, destDir: File)
                   (using
                    sh: ShellCommand => Task[Termination],
                    log: String => Task[Unit]): Task[Video] = {
    val title = unescapeHtml4(video.title)
    val tempFileName = UUID.randomUUID().toString.replace("-", "")
    val tempMP4 = file(destDir, tempFileName, "mp4")
    val tempMP3 = file(destDir, tempFileName, "mp3")
    val audioMP3 = file(destDir, s"'$title'", "mp3")
    for {
      _ <- log(s"\nProcessing: $title")
      _ <- log("Downloading..")
      _ <- sh(youtubeDL(tempMP4, video.url, "mp4"))
      _ <- log("Converting..")
      _ <- sh(ffmpeg(tempMP4, tempMP3))
      _ <- log("Finalising..")
      _ <- sh(rm(tempMP4))
      _ <- sh(mv(tempMP3, audioMP3))
      _ <- log("Done!")
    } yield video
  }
}
