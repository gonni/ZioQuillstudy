package c.y.zio.sample

import zio._
import scala.util._
import java.io.IOException

object ErrorPlayGround extends ZIOAppDefault {

//  val myApp: ZIO[Any, Nothing, Unit] =
//    Console.print("Please enter a number: ").orDie *>
//      Console.readLine.orDie
//        .map(_.toInt)
//        .map(_ % 2 == 0)
//        .flatMap {
//          case true =>
//            Console.printLine("You have entered an even number.").orDie
//          case false =>
//            Console.printLine("You have entered an odd number.").orDie
//        }

  val myApp: ZIO[Any, Nothing, Unit] =
    Console.print("Please enter a number: ").orDie *>
      Console.readLine.orDie
        .mapAttempt(_.toInt)
        .map(_ % 2 == 0)
        .flatMap {
          case true =>
            Console.printLine("You have entered an even number.").orDie
          case false =>
            Console.printLine("You have entered an odd number.").orDie
        }.catchAll(_ => myApp)

  // ---- ZIO either
  sealed trait AgeValidationException extends Exception
  case class NegativeAgeException(age: Int) extends AgeValidationException
  case class IllegalAgeException(age: Int) extends AgeValidationException

  def validate(age: Int): ZIO[Any, AgeValidationException, Int] =
    if (age < 0)
      ZIO.fail(NegativeAgeException(age))
    else if (age < 18)
      ZIO.fail(IllegalAgeException(age))
    else ZIO.succeed(age)

  val validateAge: URIO[Any, Either[AgeValidationException, RuntimeFlags]] = validate(12).either

  // --
  val myApp1: ZIO[Any, IOException, Unit] =
    for {
      _ <- Console.print("Please enter your age: ")
      age <- Console.readLine.map(_.toInt)
      res <- validate(age).either
      _ <- res match {
        case Left(error) => ZIO.debug(s"validation failed: $error")
        case Right(age) => ZIO.debug(s"The $age validated!")
      }
    } yield ()

  // ---
  def sqrt(input: ZIO[Any, Nothing, Double]): ZIO[Any, String, Double] =
    ZIO.absolve(
      input.map { value =>
        if (value < 0.0)
          Left("Value must be >= 0.0")
        else
          Right(Math.sqrt(value))
      }
    )

  val f1: ZIO[Any, String, Int] =
    ZIO.fail("Oh uh!").as(1)

  val f2: ZIO[Any, String, Int] =
    ZIO.fail("Oh error!").as(2)

  val myApp2: ZIO[Any, String, (Int, Int)] = f1 zipPar f2

  def parseInt(input: String): ZIO[Any, NumberFormatException, Int] =
    ZIO.attempt(input.toInt) // ZIO[Any, Throwable, Int]
      .refineToOrDie[NumberFormatException] // ZIO[Any, NumberFormatException, Int]

  // -- unrefine --

  import zio._

  case class Foo(msg: String) extends Throwable(msg)
  case class Bar(msg: String) extends Throwable(msg)
  case class Baz(msg: String) extends Throwable(msg)

  def unsafeOpThatMayThrows(i: String): String =
    if (i == "foo")
      throw Foo("Oh uh!")
    else if (i == "bar")
      throw Bar("Oh Error!")
    else if (i == "baz")
      throw Baz("Oh no!")
    else i

  def effect(i: String): ZIO[Any, Nothing, String] =
    ZIO.succeed(unsafeOpThatMayThrows(i))

  val unrefined: ZIO[Any, Foo, String] =
    effect("foo").unrefine { case e: Foo => e }

  def parseInt0(input: String): ZIO[Any, Option[String], Int] =
    if (input.isEmpty)
      ZIO.fail(Some("empty input"))
    else
      try {
        ZIO.succeed(input.toInt)
      } catch {
        case _: NumberFormatException => ZIO.fail(None)
      }

  def flattenedParseInt(input: String): ZIO[Any, String, Int] =
    parseInt0(input).flattenErrorOption("non-numeric input")

  // catch Exception --
//  sealed trait AgeValidationException extends Exception
//  case class NegativeAgeException(age: Int) extends AgeValidationException
//  case class IllegalAgeException(age: Int)  extends AgeValidationException

  def resultAge: ZIO[Any, Nothing, Int] =
    validate(10).catchAll{
      case NegativeAgeException(age) =>
        ZIO.debug(s"negative age: $age").as(-1)
//      case IllegalAgeException(age) =>
//        ZIO.debug(s"illegal age: $age").as(-1)
    }

  // sandbox
  val effect: ZIO[Any, String, String] =
    ZIO.succeed("primary result") *> ZIO.fail("Oh uh!")

  val myApp3: ZIO[Any, Cause[String], String] =
    effect.sandbox.catchSome {
      case Cause.Interrupt(fiberId, _) =>
        ZIO.debug(s"Caught interruption of a fiber with id: $fiberId") *>
          ZIO.succeed("fallback result on fiber interruption")
      case Cause.Die(value, _) =>
        ZIO.debug(s"Caught a defect: $value") *>
          ZIO.succeed("fallback result on defect")
      case Cause.Fail(value, _) =>
        ZIO.debug(s"Caught a failure: $value") *>
          ZIO.succeed("fallback result on failure")
    }

  val finalApp: ZIO[Any, String, String] = myApp3.unsandbox.debug("final result")

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = myApp2.debug

//    ZIO
//    .fail("Oh uh!")
//    .catchAllTrace {
//      case ("Oh uh!", trace)
//        if trace.toJava
//          .map(_.getLineNumber)
//          .headOption
//          .contains(4) =>
//        ZIO.debug("caught a failure on the line number 4")
//      case _ =>
//        ZIO.debug("caught other failures")
//    }
//    resultAge.debug
  //parseInt("10a").debug



}
