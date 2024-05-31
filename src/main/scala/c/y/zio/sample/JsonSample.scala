package c.y.zio.sample

import zio._
import zio.json._

object JsonSample {
  def main(args: Array[String]): Unit = {
    val data = """{"curvature":0.5}""".fromJson[Banana]
    println(data)

    val js2str = Banana(0.5).toJsonPretty
    println(js2str)
  }
}

sealed trait Fruit
case class Banana(curvature: Double) extends Fruit
case class Apple (poison: Boolean)   extends Fruit

object Fruit {
  implicit val decoder: JsonDecoder[Fruit] = DeriveJsonDecoder.gen[Fruit]
  implicit val encoder: JsonEncoder[Fruit] = DeriveJsonEncoder.gen[Fruit]
}

object Banana {
  implicit val decoder: JsonDecoder[Banana] = DeriveJsonDecoder.gen[Banana]
  implicit val encoder: JsonEncoder[Banana] = DeriveJsonEncoder.gen[Banana]
}