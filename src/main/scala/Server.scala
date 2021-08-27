import shell.util.Command.{ShellCommand, Termination}
import data.Config
import shell.util.Command
import storage.util.FileSystem
import http.util.{Http, URL}

import java.io.File
import zio._
import com.ravram.nemesis.Json
import io.javalin.Javalin
import io.javalin.http.Context
import scala.util.chaining._
import java.time.DayOfWeek

val schedule = Schedule.dayOfWeek(DayOfWeek.WEDNESDAY.getValue) && Schedule.forever

def handle[A](task: Core.OTask[A]): Core.OTask[A] = task.foldM(
  err => Task.effect(err.printStackTrace).flatMap(_ => Task.fail(err)),
  res => Core.succeed(res)
)

def playlistCreation: Unit = {
  given conf: Task[Config] = Config.config

  given sh: (ShellCommand => Task[Termination]) = Command.execute

  given slurp: (File => Task[Option[String]]) = FileSystem.slurp

  given spit: ((String, File) => Task[Unit]) = FileSystem.spit

  given get: (URL => Task[Json]) = Http.get

  given log: (String => Task[Unit]) = msg => Task.succeed(println(msg))

  Runtime.default.unsafeRunAsync(handle(Core.process.schedule(schedule))) {
    case Exit.Success(v) => println("Job completed successfully")
    case Exit.Failure(cause) => println("Scheduling failed for some reason, take a look at the logs")
  }
}

def storeChannel(channelId: String): String = {
  given config: Task[Config] = Config.config

  given log: (String => Task[Unit]) = msg => Task.effect(println(msg))

  given get: (URL => Task[Json]) = Http.get

  given spit: ((String, File) => Task[Unit]) = FileSystem.spit

  given slurp: (File => Task[Option[String]]) = FileSystem.slurp

  Runtime.default.unsafeRunSync(handle(Core.store(channelId))) match {
    case Exit.Success(_) => "Stored channel successfully"
    case Exit.Failure(_) => "Storing failed for some reason, take a look at the logs"
  }
}

def getChannels: String = {
  given config: Task[Config] = Config.config

  given slurp: (File => Task[Option[String]]) = FileSystem.slurp

  given log: (String => Task[Unit]) = msg => Task.succeed(println(msg))

  Runtime.default.unsafeRunSync(handle(Core.channels)) match {
    case Exit.Success(json) => json.encode
    case Exit.Failure(_) => "Something went wrong"
  }
}

@main def runServer: Unit = Javalin
  .create()
  .start(8080)
  .post("/job", ctx => playlistCreation)
  .post("/channels/:id", ctx => storeChannel(ctx.pathParam("id")))
  .get("/channels", ctx => ctx.result(getChannels))
  .get("/oauth2/code", ctx => ctx.queryParam("code").pipe(println))