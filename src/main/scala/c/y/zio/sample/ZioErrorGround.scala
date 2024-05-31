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


  def isPrime(n: Int): Boolean =
    if (n <= 1) false else (2 until n).forall(i => n % i != 0)

  def findPrimeBetween(
                        minInclusive: Int,
                        maxExclusive: Int
                      ): ZIO[Any, List[String], Int] =
    for {
      errors <- Ref.make(List.empty[String])
      number <- Random
        .nextIntBetween(minInclusive, maxExclusive)
        .reject {
          case n if !isPrime(n) =>
            s"non-prime number rejected: $n"
        }
        .flatMapError(error => errors.updateAndGet(_ :+ error))
        .retryUntil(_.length >= 5)
    } yield number

  val myApp3: ZIO[Any, Nothing, Unit] =
    findPrimeBetween(1000, 10000)
      .flatMap(prime => Console.printLine(s"found a prime number: $prime").orDie)
      .catchAll { (errors: List[String]) =>
        Console.printLine(
          s"failed to find a prime number after 5 attempts:\n  ${errors.mkString("\n  ")}"
        )
      }
      .orDie

  val evens: ZIO[Any, List[String], List[Int]] =
    ZIO.validate(List(1, 2, 3, 4, 5)) { n =>
      if (n % 2 == 0)
        ZIO.succeed(n)
      else
        ZIO.fail(s"$n is not even")
    }

  val r1: ZIO[Any, List[String], List[Int]] = evens.mapError(_.reverse)
  val r2: ZIO[Any, List[String], List[Int]] = evens.flip.map(_.reverse).flip
  val r3: ZIO[Any, List[String], List[Int]] = evens.flipWith(_.map(_.reverse))

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Random
    .nextIntBounded(20)
    .reject {
      case n if n % 2 == 0 => s"even number rejected: $n"
      case 5               => "number 5 was rejected"
    }
    .debug

//    parseInt("1").orElseOptional(ZIO.succeed(0)).debug
//    validate(3).orElseSucceed(100).debug


}
