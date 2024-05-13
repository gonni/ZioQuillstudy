package c.y.zio.sample

import zio.ZIOAppDefault
import zio._

import java.io.IOException
import java.net.NoRouteToHostException
import scala.util._

class ZioEroorHandling extends ZIOAppDefault {

  val aFailedZIO: IO[String, Nothing] = ZIO.fail("Something went wrong")
  val failedWithThroable: IO[RuntimeException, Nothing] = ZIO.fail(new RuntimeException("Bumb!"))
  val failWithDescription: ZIO[Any, String, Nothing] = failedWithThroable.mapError(_.getMessage)

  val badZIO: ZIO[Any, Nothing, RuntimeFlags] = ZIO.succeed {
    println("Trying something")
    val string: String = null
    string.length
  }

  val anAttempt: ZIO[Any, Throwable, Int] = ZIO.attempt {
    println("Trying something")
    val string: String = null
    string.length
  }

  // effectfully catch error
  val catchError: ZIO[Any, Throwable, Any] =
    anAttempt.catchAll(a => ZIO.attempt(s"Returning a different value because $a"))

  val catchServiceErrors: ZIO[Any, Throwable, Any] = anAttempt.catchSome {
    case e: RuntimeException => ZIO.succeed(s"Ignoring runtime excep[tion: $e")
    case _ => ZIO.succeed("Ignoring everything else")
  }

  // chain effects
  val aBetterAttempt: ZIO[Any, Nothing, Int] = anAttempt.orElse(ZIO.succeed(56))

  val handleBoth: URIO[Any, String] = anAttempt.fold(ex => s"Something bad happends: $ex", value => s"Length of the string was $value")

  val handleBoth_v2: ZIO[Any, Nothing, String] = anAttempt.foldZIO(
    ex => ZIO.succeed(s"Something bad happends: $ex"),
    value => ZIO.succeed(s"Length of the string was $value")
  )

  val aTryToZIO: Task[Int] = ZIO.fromTry(Try(4 / 0))

  val anEither: Either[Int, String] = Right("Success!")
  val anEitherToZIO: ZIO[Any, Int, String] = ZIO.fromEither(anEither)
  // ZIO -> ZIO with Either as the value channel
  val eitherZIO: URIO[Any, Either[Throwable, Int]] = anAttempt.either

  val anAttempt_ve: ZIO[Any, Throwable, Int] = eitherZIO.absolve

  //option -> ZIO
  val anOption: ZIO[Any, Option[Nothing], Int] = ZIO.fromOption(Some(42))

  // fold is perfect

  def try2ZIO[A](aTry: Try[A]): Task[A] = aTry match {
    case Failure(exception) => ZIO.fail(exception)
    case Success(value) => ZIO.succeed(value)
  }

  def either2ZIO[A, B](anEither: Either[A, B]): ZIO[Any, A, B] = anEither match {
    case Left(value) => ZIO.fail(value)
    case Right(value) => ZIO.succeed(value)
  }

  def option2ZIO[A](anOption: Option[A]): ZIO[Any, Option[Nothing], A] = anOption match {
    case Some(value) => ZIO.succeed(value)
    case None => ZIO.fail(None)
  }

  def zio2zioEither[R,A,B](zio: ZIO[R, A, B]): ZIO[R, Nothing, Either[A, B]] = zio.foldZIO(
    error => ZIO.succeed(Left(error)),
    value => ZIO.succeed(Right(value))
  )

  def absolveZIO[R,A,B](zio: ZIO[R, Nothing, Either[A, B]]): ZIO[R, A, B] = zio.flatMap {
    case Left(e) => ZIO.fail(e)
    case Right(v) => ZIO.succeed(v)
  }

  // --- chapter 2 ---
  /*
  ZIO[R,E,A] can finish with Exit[E,A]
  - Success[A] containing a value
  - Cause[E]
    - Fail[E] containing the error
    - Die(t: Throwable) which was unforeseen
  */

  val divisionByZero: UIO[Int] = ZIO.succeed(1 / 0)

  val failedInt: ZIO[Any,String, Int] = ZIO.fail("I failed!")
  val failureCauseExposed: ZIO[Any, Cause[String], Int] = failedInt.sandbox
  val failureCauseHidden: ZIO[Any, String, Int] = failureCauseExposed.unsandbox
  // fold with cause
  val foldedWithCause: URIO[Any, String] = failedInt.foldCause(
    cause => s"this failed with ${cause.defects}",
    value => s"this succeeded with $value"
  )

  val foldedWithCause_v2: ZIO[Any, Nothing, String] = failedInt.foldCauseZIO(
    cause => ZIO.succeed(s"this failed with ${cause.defects}"),
    value => ZIO.succeed(s"this succeeded with $value")
  )

  // --
  def callHttpEndpoint(url: String): ZIO[Any, IOException, String] =
    ZIO.fail(new IOException("no internet, dummy!"))

  val endpointCollWithDefects: ZIO[Any, Nothing, String] =
    callHttpEndpoint("naver.com").orDie

  def callHTTPEndpopintWideError(url:String): ZIO[Any, Exception, String] =
    ZIO.fail(new IOException("No internet"))

  //refine
  def callHttpEndpoiting_v2(url: String): ZIO[Any, IOException, String] =
    callHTTPEndpopintWideError(url).refineOrDie[IOException] {
      case e: IOException => e
      case _: NoRouteToHostException => new IOException(s"No route to host to $url, can't fetch page")
    }

  //unrefine - reverse
  val endpointCallWithError: ZIO[Any, String, String] = endpointCollWithDefects.unrefine{
    case e => e.getMessage
  }

  // Exercises
  val aBadFailures: ZIO[Any, Nothing, Int] = ZIO.succeed[Int](throw new RuntimeException("this is bad!"))
  val aBetterFailure: ZIO[Any, Cause[Nothing], Int] = aBadFailures.sandbox
  val aBetterFailure_v2: ZIO[Any, Serializable, Int] = aBetterFailure.unrefine {
    case ioe: IOException => ioe
  }

  def left[R, E, A, B](zio: ZIO[R, E, Either[A, B]]): ZIO[R, Either[E, A], B] =
    zio.foldZIO(
      e => ZIO.fail(Left(e)),
      either => either match {
        case Left(a) => ZIO.fail(Right(a))
        case Right(b) => ZIO.succeed(b)
      }
    )


  override def run = ???
}
