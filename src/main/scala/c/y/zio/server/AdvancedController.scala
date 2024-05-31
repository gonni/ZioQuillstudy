package c.y.zio.server

import scala.util.Try

import zio._

import zio.http._
import zio.http.netty.NettyConfig
import zio.http.netty.NettyConfig.LeakDetectionLevel

object AdvancedController extends ZIOAppDefault {

  val PORT = 58080

  val fooBar =
    Routes(
      Method.GET / "foo" -> Handler.from(Response.text("bar")),
      Method.GET / "bar" -> Handler.from(Response.text("foo")),
    ).toHttpApp

  val app = Routes(
    Method.GET / "random" -> handler(Random.nextString(10).map(Response.text(_))),
    Method.GET / "utc" -> handler(Clock.currentDateTime.map(s => Response.text(s.toString))),
  ).toHttpApp

  val run = ???
//    ZIOAppArgs.getArgs.flatMap { args =>
//    // Configure thread count using CLI
//    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)
//
//    val config = Server.Config.default
//      .port(PORT)
//    val nettyConfig = NettyConfig.default
//      .leakDetection(LeakDetectionLevel.PARANOID)
//      .maxThreads(nThreads)
//    val configLayer = ZLayer.succeed(config)
//    val nettyConfigLayer = ZLayer.succeed(nettyConfig)
//
//    (fooBar ++ app)
////      .serve[Any]
////      .provide(configLayer, nettyConfigLayer, Server.customized)
//  }

}
