package c.y.zio.v

import zio.*
import zio.http.*
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

object ApiController {

  import CustomMiddleware._

  def apply(): Routes[UserRepo, Response] = Routes(
    Method.POST / "users" -> handler { (req: Request) =>
      for{
        u <- req.body.to[User].orElseFail(Response.badRequest)
        r <- UserRepo.register(u).mapBoth(
          e => {
            println("Error detected in insertion ..")
            e.printStackTrace()
            Response.internalServerError
          },
          id => Response.text(id)
        )
      } yield r
    },

    Method.GET / "users" / string("id") -> handler { (id: String, _: Request) =>
      UserRepo
        .lookup(id)
        .mapBoth(
          e => {
            println("Error Detected in user one")
            e.printStackTrace()
            Response.internalServerError(s"Cannot retrieve user $id")
          },
          {
            case Some(user) =>
              Response(body = Body.from(user))
            case None =>
              Response.text(s"User $id not found!")
          }
        )
    },

    Method.GET / "users" -> handler {
      for {
        _ <- ZIO.logInfo("[FOR-LOG] R`equest All Users")
        user <-  UserRepo.users.mapBoth(
              e => {
              println ("Error Detected in user all")
              e.printStackTrace ()
              Response.internalServerError ("Cannot retrieve users!")
              } ,
              users => Response(body = Body.from(users))
        )
      } yield user
    } //@@ logSpan("span-all-users")
  ) @@ logAnnotateCorrelationId
}
