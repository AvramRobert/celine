package misc

import com.ravram.nemesis.{Attempt, Read, Write}

object Json {
  def read[C, A](using reader: ReadFrom[C, A]): Read[A] = reader.reads
  def write[C, A](using writer: WriteTo[C, A]): Write[A] = writer.writes


  final case class ReadFrom[C, A](reads: Read[A])
  final case class WriteTo[C, A](writes: Write[A])
}