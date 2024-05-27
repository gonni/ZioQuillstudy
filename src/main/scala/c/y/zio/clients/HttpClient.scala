package c.y.zio.clients

import zio._
import zio.http.*
import zio.schema.{DeriveSchema, Schema}
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

case class Todo(
                 userId: Int,
                 id: Int,
                 title: String,
                 completed: Boolean,
               )

object Todo {
  implicit val todoSchema: Schema[Todo] = DeriveSchema.gen[Todo]
}

object HttpClient extends ZIOAppDefault{
  val program: ZIO[Client & Scope, Throwable, Unit] =
    for {
      res <- Client.request(Request.get("http://jsonplaceholder.typicode.com/todos"))
      data <- res.body.asString
      _ <- Console.printLine(s"Response String -> ${data}")
//      todos <- res.body.to[List[Todo]]
//      _ <- Console.printLine(s"The first task is '${todos.head.title}'")
    } yield  ()

  override val run = program.provide(Client.default, Scope.default).debug
}
