package c.y.zio.v

import zio.*
import zio.http.*

import java.util.Date

object HellRoutes {
  
  def apply(): Routes[Any, Response] = {
    Routes(
      Method.GET / "hell" -> handler { (req: Request) =>
        for {
          _ <- ZIO.logInfo("Detect Hell Request ..")
          res <- ZIO.succeed(Response.text("hella at " + new Date()))
        } yield res
      }
    )
  } @@ Middleware.basicAuth("hell", "pw")
}
