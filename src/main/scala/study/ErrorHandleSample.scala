package study

import zio._

object ErrorHandleSample extends ZIOAppDefault {


  import zio._

  def parseInt(input: String): ZIO[Any, NumberFormatException, Int] =
    ZIO.attempt(input.toInt).mapError(_ => new NumberFormatException("Ivalid Input Source: " + input))

  // mapping the error of the original effect to its message
  val r1: ZIO[Any, String, Int] =
    parseInt("five") // ZIO[Any, NumberFormatException, Int]
      .mapError(e => e.getMessage) // ZIO[Any, String, Int]

  // mapping the cause of the original effect to be untraced
  val r2: ZIO[Any, NumberFormatException, RuntimeFlags] =
    parseInt("five") // ZIO[Any, NumberFormatException, Int]
      .mapErrorCause(_.untraced) // ZIO[Any, NumberFormatException, Int]

  val r2catched: ZIO[Any, String, Int] = r2.catchAll(_ => ZIO.succeed(-1))

  val r3failed: IO[String, Nothing] = ZIO.fail("error")
  val r3: ZIO[Any, Nothing, String] = r3failed.orElse(ZIO.succeed("default"))
  // ---

  val r4: ZIO[Any, Nothing, String] = ZIO.fail("error").catchAll(_ =>
    for {
      _ <- ZIO.logError("Some error occurred")
      resource <- ZIO.succeed("default")
    } yield resource
  )

  val effect1 =
    ZIO.dieMessage("Boom!") // ZIO[Any, Nothing, Nothing]
      .absorb // ZIO[Any, Throwable, Nothing]
      .ignore
  val effect2 =
    ZIO.interrupt // ZIO[Any, Nothing, Nothing]
      .absorb // ZIO[Any, Throwable, Nothing]
      .ignore

  val result: ZIO[Any, String, Int] =
    Console.readLine.orDie.mapAttempt(_.toInt).mapBoth(
      _ => "non-integer input",
      n => Math.abs(n)
    )

  val multiCatched: ZIO[Any, Serializable, (Int, String)] = parseInt("66") <*> parseInt("5").mapBoth(
    error => "ERROR failed: " + error,
    succeed => "" + succeed
  )

  val sum = multiCatched.map{(num, s) =>
    num + s.toInt
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = sum.debug

}
