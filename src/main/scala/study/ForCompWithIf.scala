package study

object ForCompWithIf {

  def main(args: Array[String]): Unit = {
    case class User(name: String, age: Int)

    val userBase = List(
      User("Travis", 28),
      User("Kelly", 33),
      User("Jennifer", 44),
      User("Dennis", 23))

    val sample = List(1 ,2)

    val twentySomethings =
      for {
        user <- userBase if user.age >= 20 && user.age < 30
        i <- sample
      } yield (user.name, i) // i.e. add this to a list

    twentySomethings.foreach(println) // prints Travis Dennis
  }
}
