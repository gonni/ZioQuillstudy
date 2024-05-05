package c.y.zio.db

import zio._
import zio.http.{HttpApp, Routes, handler, int, _}
import zio.config.typesafe._
import zio.json._

object HelloServer extends ZIOAppDefault {

  val app: HttpApp[Any] =
    Routes(
      Method.GET / "text" -> handler(Response.text("Hello ZIO World!")),
      Method.GET / "Apple" / int("count") -> handler{
        (count: Int, req: Request) =>
          println("Detected Applie :" + count)
          Response.text(s"Apple: $count")
      },
      Method.POST / "echo" ->
        handler{(req: Request) =>
          req.body.asString.map(Response.text(_))
        }.sandbox,
      Method.GET / "x" ->
        Handler.fromResponseZIO(Random.nextUUID.map(u => Response.text(u.toString))),
      Method.GET / "j" / int("userId") -> {
        Handler.fromFunction[(Int, Request)] { case (userId: Int, request: Request) =>
          Response.json(
            Map(
              "user" -> userId.toString,
              "correlationId" -> request.headers.get("X-Correlation_ID").get,
            ).toJsonPretty
          )
        } // *> Handler.fail(new Error("XXX"))
      }
    ).toHttpApp

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(ConfigProvider.fromResourcePath())

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Server.serve(app).provide(Server.configured()).debug

}
