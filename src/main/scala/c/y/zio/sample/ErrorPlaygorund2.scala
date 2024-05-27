package c.y.zio.sample

import zio.*

import java.io.IOException

object ErrorPlaygorund2 extends ZIOAppDefault {

  val iFailed: Task[Int] = ZIO.attempt {
    val target: String = null
    target.length
  }

  val iFailedSandboxed: ZIO[Any, Cause[Throwable], RuntimeFlags] = iFailed.sandbox

  val perfectFailed: ZIO[Any, String, Nothing] = ZIO.fail("This is Failed Effect ..")

  val perfectFailedSandbox: ZIO[Any, Cause[String], Nothing] = perfectFailed.sandbox
  val perfectFailedUnsandbox: ZIO[Any, String, Nothing] = perfectFailedSandbox.unsandbox

  // ---
  sealed trait AgeValidationException extends Exception
  case class NegativeAgeException(age: Int) extends AgeValidationException
  case class IllegalAgeException(age: Int) extends AgeValidationException

  def validate(age: Int): ZIO[Any, AgeValidationException, Int] =
    if (age < 0)
      ZIO.fail(NegativeAgeException(age))
    else if (age < 18)
      ZIO.fail(IllegalAgeException(age))
    else ZIO.succeed(age)

  def superValidate(age: Int): ZIO[Any, Nothing, Int] = validate(age).catchAll{
    case _: Exception => ZIO.succeed(-1)
  }

  def divide10By(b: Int): Option[Int] =
    try {
      Some(10/b)
    } catch {
      case _: IllegalArgumentException => None
    }

  def divide10ByZIO(v: Int): ZIO[Any, Throwable, Option[Int]] = ZIO.attempt{
    divide10By(v)
  }

//  def printDiv10By(v: Int) = divide10ByZIO(v) match {
//    case Some(v) => println(v)
//  }

 val x1: ZIO[Any, IOException, Unit] = ZIO.fail("e1")
    .ensuring(ZIO.succeed(throw new Exception("e2")))
    .catchAll {
      case "e1" => Console.printLine("e1")
      case "e2" => Console.printLine("e2")
    }


   def play(div: Int): ZIO[Any, Throwable, Option[Int]] =
     divide10ByZIO(div).catchSome {
       case e: RuntimeException => ZIO.succeed(Some(-1))
       case _ => ZIO.succeed(None)
     }

  val result: ZIO[Any, Nothing, Int] =
    validate(2)
      .catchAll {
        case NegativeAgeException(age) =>
          ZIO.debug(s"negative age: $age").as(-1)
//        case IllegalAgeException(age) =>
//          ZIO.debug(s"illegal age: $age").as(-1)
      }

 override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = result.debug


}
