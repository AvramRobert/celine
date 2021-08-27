package misc

import com.ravram.nemesis.{Read, Write}

object Json {
  case class ReadFrom[C, A](reads: Read[A])
  case class WriteTo[C, A](writes: Write[A])
}