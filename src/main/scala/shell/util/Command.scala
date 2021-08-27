package shell.util

import http.util.URL
import java.io.File
import sys.process._
import zio.Task

object Command {
  opaque type ShellCommand = String

  enum Termination:
    case Graceful
    case Errornous
    case Other(code: Int)

  def execute(command: ShellCommand): Task[Termination] = Task.effect {
    command.run().exitValue() match {
      case -1 => Termination.Errornous
      case 0 => Termination.Graceful
      case n => Termination.Other(n)
    }
  }

  def youtubeDL(output: File, videoURL: URL, videoExt: String): ShellCommand =
    s"youtube-dl -f 'best[ext=$videoExt]' -o '$output' ${videoURL.encode}"

  def ffmpeg(from: File, to: File): ShellCommand =
    s"ffmpeg -hide_banner -loglevel error -i ${from.toPath.toAbsolutePath} -n ${to.toPath.toAbsolutePath}"
    
  def rm(file: File, recursive: Boolean = false): ShellCommand =
    s"rm ${if (recursive) then "-r" else ""} ${file.toPath.toAbsolutePath}"
    
  def mv(from: File, to: File): ShellCommand =
    s"mv ${from.toPath.toAbsolutePath} ${to.toPath.toAbsolutePath}"
    
  def ls(file: File): ShellCommand =
    s"ls ${file.toPath.toAbsolutePath}"
}
