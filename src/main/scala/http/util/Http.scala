package http.util

import com.ravram.nemesis.Json
import sttp.client3._
import http.util.URL
import misc.Extensions._
import zio.Task

import java.nio.file.Paths

object Http {
  lazy val backend = HttpURLConnectionBackend()

  def get(url: URL): Task[Json] = {
    println(url.encode)
    basicRequest
      .get(uri"${url.encode}")
      .send(backend)
      .body
      .fold(err => Task.fail(Throwable(err)), json => Task.succeed(json))
      .map(Json.forceParse)
      .flatMap(_.task)
  }
}
