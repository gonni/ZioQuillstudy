package c.y.zio.basic

import zio._

object ErrorSample extends ZIOAppDefault {

  val f1: IO[String, Nothing] = ZIO.fail("Oh uh!")
  val f2: ZIO[Any, String, Int] = ZIO.succeed(5) *> ZIO.fail("Oh uh!")

  val f3: ZIO[Any, Exception, Nothing] = ZIO.fail(new Exception("Oh uh!"))

  case class NegativeNumberException(msg: String) extends Exception(msg)

  def validateNonNegative(input: Int): ZIO[Any, NegativeNumberException, Int] =
    if (input < 0)
      ZIO.fail(NegativeNumberException(s"entered negative number: $input"))
    else
      ZIO.succeed(input)

  def catchedOrZero(num: Int): ZIO[Any, String, Int] = validateNonNegative(num).mapError(e => "Invalid Number :" + num)

  def cachedOrDie(num: Int) = catchedOrZero(num)

  def divide(a: Int, b: Int): ZIO[Any, Nothing, Int] =
    if (b == 0)
      ZIO.die(new ArithmeticException("divide by zero")) // Unexpected error
    else
      ZIO.succeed(a / b)

  def divide2(a: Int, b: Int): ZIO[Any, Nothing, Int] =
    ZIO.succeed(a / b)

  val defect4: ZIO[Any, Nothing, Nothing] = ZIO.succeed(???).map(_ => throw new Exception("Boom!"))
  val defect5: ZIO[Any, Throwable, Nothing] = ZIO.attempt(???).map(_ => throw new Exception("Boom!"))

  val defect6: ZIO[Any, Nothing, String] = ZIO.succeed(throw new Exception("Un Oh!!!"))

  val dyingEffect: ZIO[Any, Nothing, Nothing] =
    ZIO.die(new ArithmeticException("divide by zero"))

  import zio._

  val effect1 =
    ZIO.fail(new IllegalArgumentException("wrong argument")) // ZIO[Any, IllegalArgumentException, Nothing]
      .orDie // ZIO[Any, Nothing, Nothing]
      .absorb // ZIO[Any, Throwable, Nothing]
      .refineToOrDie[IllegalArgumentException] // ZIO[Any, IllegalArgumentException, Nothing]

  val effect2 =
    ZIO.fail(new IllegalArgumentException("wrong argument")) // ZIO[Any, IllegalArgumentException , Nothing]
      .orDie // ZIO[Any, Nothing, Nothing]
//      .resurrect // ZIO[Any, Throwable, Nothing]
//      .refineToOrDie[IllegalArgumentException] // ZIO[Any, IllegalArgumentException, Nothing]


  val finalizer =
    ZIO.succeed(println("Finalizing!"))

  val finalized: IO[String, Unit] =
    ZIO.fail("Failed!").ensuring(finalizer)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = finalized.debug
//    Console.printLine("Hell World").debug
    //divide2(10, 0).debug
//    catchedOrZero(-10).debug
  //validateNonNegative(-10).mapError(e => 10)
}
