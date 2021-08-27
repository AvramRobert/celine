package data

object Downloads {}

final case class Downloads(succeeded: Vector[Video], failed: Vector[Video])
