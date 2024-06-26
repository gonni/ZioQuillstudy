package c.y.zio.server

import java.util.concurrent.TimeUnit
import zio._
import zio.http._

object MwConstroller extends ZIOAppDefault {

  val app = Routes(
    // this will return result instantly
    Method.GET / "text" -> handler(ZIO.succeed(Response.text("Hello World!"))),
    // this will return result after 5 seconds, so with 3 seconds timeout it will fail
    Method.GET / "long-running" -> handler(ZIO.succeed(Response.text("Hello World!")).delay(Duration.fromSeconds(5))),
  ).toHttpApp

  val serverTime = Middleware.patchZIO(_ =>
    for {
      currentMilliseconds <- Clock.currentTime(TimeUnit.MILLISECONDS)
      header = Response.Patch.addHeader("X-Time", currentMilliseconds.toString)
    } yield header,
  )
  val middlewares =
    // print debug info about request and response
    Middleware.debug ++
      // close connection if request takes more than 3 seconds
      Middleware.timeout(Duration.fromSeconds(3)) ++
      // add static header
      Middleware.addHeader("X-Environment", "Dev") ++
      // add dynamic header
      serverTime

  // Run it like any simple app
  val run = Server.serve(app @@ middlewares).provide(Server.default)
}
