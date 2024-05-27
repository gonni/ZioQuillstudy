package c.y.zio.server

import zio.*
import zio.config.typesafe.*
import zio.http.*
import zio.json.*

//object EchoSample extends ZIOAppDefault {
//  val app: HttpApp[Any] = Routes(
//      Method.POST / "echo" ->
//        handler { (req: Request) =>
//          req.body.asString(Charsets.Utf8).map(Response.text(_)).sandbox
//        }
//    ).toHttpApp
//
//  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
//    Runtime.setConfigProvider(ConfigProvider.fromResourcePath())
//
//  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
//    Server.serve(app).provide(Server.configured()).debug
//}
