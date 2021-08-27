package misc

import com.ravram.nemesis.Attempt
import zio.Task

object Extensions {
  extension [E, A] (nem: Attempt[A])
    def task: Task[A] = nem.fold[Task[A]](a => Task.succeed(a), err => Task.fail(Throwable(err)))
}
