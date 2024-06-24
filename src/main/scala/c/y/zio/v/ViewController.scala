package c.y.zio.v

import zio.*
import zio.http.*

import java.util.Date

object ViewController {
  def apply(): Routes[UserRepo, Response] = Routes(
    Method.GET / "hella" -> handler { (req: Request) => 
      Response.text("Hella at " + new Date())
    }
  )
}
