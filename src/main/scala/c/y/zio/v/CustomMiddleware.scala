package c.y.zio.v

import zio._
import zio.http._

object CustomMiddleware {
  val correlationId = Middleware.logAnnotate{ req =>
    val correlationId = req.headers.get("X-Correlation_ID").getOrElse(
      Unsafe.unsafe{implicit unsafe =>
        Runtime.default.unsafe.run(Random.nextUUID.map(_.toString)).getOrThrow()
      }
    )
    Set(LogAnnotation("correlation-id", correlationId))
  }

//  val autoLogger: Middleware[Any] = new Middleware[Any]:
//    override def apply[Env1 <: Any, Err](app: Routes[Env1, Err]): Routes[Env1, Err] =
//      app.transform { h =>
//        handler {(req: Request) =>
//          ZIO.logAnnotate("random")
//        }
//      }

  def logAnnotateCorrelationId: Middleware[Any] =
    new Middleware[Any] {
      override def apply[Env1 <: Any, Err](
                                            app: Routes[Env1, Err]
                                          ): Routes[Env1, Err] =
        app.transform { h =>
          handler { (req: Request) =>
            def correlationId(req: Request): UIO[String] =
              ZIO
                .succeed(req.headers.get("X-Correlation-ID"))
                .flatMap(x =>
                  Random.nextUUID.map(uuid => x.getOrElse(uuid.toString))
                )

            for {
              _ <- ZIO.logInfo("AA")
              res <- correlationId(req).flatMap(id =>
                ZIO.logAnnotate("correlation-id", id)(h(req))
              )
              _ <- ZIO.logInfo("BB")
            } yield res
          }
        }
    }

  def logSpan2(
             label: String
           ): ZIOAspect[Nothing, Any, Nothing, Any, Nothing, Any] =
  new ZIOAspect[Nothing, Any, Nothing, Any, Nothing, Any] {
    override def apply[R, E, A](zio: ZIO[R, E, A])(
      implicit trace: Trace
    ): ZIO[R, E, A] =
      ZIO.logSpan(label)(zio)
  }

  def logSpan(label: String): HandlerAspect[Any, Unit] =
    HandlerAspect.interceptIncomingHandler {
      Handler.fromFunctionZIO{ (req: Request) =>
        ZIO.logSpan(label){
          for {
            _ <- ZIO.log("processing all users")
            res <- ZIO.succeed(req).map(r => (r, ()))
          } yield res
        }
      }
    }

  val whitelistMiddleware: HandlerAspect[Any, Unit] =
    HandlerAspect.interceptIncomingHandler {
      val whitelist = Set("127.0.0.1", "0.0.0.0")
      Handler.fromFunctionZIO[Request] { request =>
        request.headers.get("X-Real-IP") match {
          case Some(host) if whitelist.contains(host) =>
            ZIO.succeed((request, ()))
          case _ =>
            ZIO.fail(Response.text(s"Your IP:${request.headers.get("X-Real-IP")} " +
              s"is banned from accessing the server."))
        }
      }
    }

}
