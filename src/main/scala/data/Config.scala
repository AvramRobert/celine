package data

import com.typesafe.config.ConfigFactory
import data.Channel
import zio.Task

import java.io.File
import scala.jdk.CollectionConverters._

object Config {

  val config: Task[Config] = Task.effect {
    val config = ConfigFactory.load()
    Config(
      databaseFile = File(config.getString("database.file")),
      outputDirectory  = File(config.getString("output.directory")),
      apiKey = config.getString("youtube.api.key"))
  }
}

final case class Config(databaseFile: File, outputDirectory: File, apiKey: String)
