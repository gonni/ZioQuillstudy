package c.y.zio.sample

import zio._

object ZioErrorGround extends ZIOAppDefault {

  sealed trait AgeValidationException extends Exception
  case class NegativeAgeException(age: Int) extends AgeValidationException
  case class IllegalAgeException(age: Int)  extends AgeValidationException

  def validate(age: Int): ZIO[Any, AgeValidationException, Int] =
    if (age < 0)
      ZIO.fail(NegativeAgeException(age))
    else if (age < 18)
      ZIO.fail(IllegalAgeException(age))
    else ZIO.succeed(age)

  val result: ZIO[Any, Nothing, Int] =
    validate(15).catchAll{
      case NegativeAgeException(age) =>
        ZIO.debug(s"negative age: $age").as(-1)
      case IllegalAgeException(age) =>
        ZIO.debug(s"illegal age: $age").as(-2)
    }

  val myApp: ZIO[Any, String, Unit] =
    for {
      f1 <- ZIO.fail("Oh uh!").fork
      f2 <- ZIO.dieMessage("Boom!").fork
      _ <- (f1 <*> f2).join
    } yield ()

  val failedApp = ZIO.failCause(Cause.die(new Throwable("Boom!"))).cause
  val unExpectedFail = ZIO.succeed(10/0)

  val reBirth: ZIO[Any, Nothing, Unit] = ZIO.dieMessage("Boom!")
    .catchAllDefect {
      case e: RuntimeException if e.getMessage == "Boom!" =>
        ZIO.debug("Boom! defect caught.")
      case _: NumberFormatException =>
        ZIO.debug("NumberFormatException defect caught.")
      case _ =>
        ZIO.debug("Unknown defect caught.")
    }

  def parseInt(input: String): ZIO[Any, Option[String], Int] =
    input.toIntOption match {
      case Some(value) => ZIO.succeed(value)
      case None =>
        if (input.trim.isEmpty)
          ZIO.fail(None)
        else
          ZIO.fail(Some(s"invalid non-integer input: $input"))
    }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    parseInt("1").orElseOptional(ZIO.succeed(0)).debug
//    validate(3).orElseSucceed(100).debug


}
