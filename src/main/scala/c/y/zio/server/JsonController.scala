package c.y.zio.server

import zio.*
import zio.config.typesafe.*
import zio.http.*
import zio.json.*

object JsonController extends ZIOAppDefault {

  // Responds with plain text
  val homeRoute =
    Method.GET / "" -> handler(Response.text("Hello World!"))

  // Responds with JSON
  val jsonRoute =
    Method.GET / "json" -> handler(Response.json(Banana(33).toJsonPretty))

  // Create HTTP route
  val app = Routes(homeRoute, jsonRoute).toHttpApp

  // Run it like any simple app
  override val run = Server.serve(app).provide(Server.default)
}

//sealed trait Fruit
case class Banana(curvature: Double)  // extends Fruit
//case class Apple (poison: Boolean)   extends Fruit
//
//object Fruit {
//  implicit val decoder: JsonDecoder[Fruit] = DeriveJsonDecoder.gen[Fruit]
//  implicit val encoder: JsonEncoder[Fruit] = DeriveJsonEncoder.gen[Fruit]
//}
object Banana {
  implicit val decoder: JsonDecoder[Banana] = DeriveJsonDecoder.gen[Banana]
  implicit val encoder: JsonEncoder[Banana] = DeriveJsonEncoder.gen[Banana]
}