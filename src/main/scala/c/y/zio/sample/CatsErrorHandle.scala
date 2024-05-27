package c.y.zio.sample

import cats.{Applicative, Monad}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object CatsErrorHandle {

  trait MyApplicativeError[M[_], E] extends Applicative[M] {
    def raiseError[A](e: E): M[A]
    def handleErrorWith[A](ma: M[A])(func: E => M[A]): M[A]
    def handleError[A](ma: M[A])(func:E => A): M[A] = handleErrorWith(ma)(a => pure(func(a)))
  }

  trait MyMonadError[M[_], E] extends Monad[M] {
//    def raiseError[A](e: E): M[A]
    def ensure[A](ma: M[A])(error: E)(predicate: A => Boolean): M[A]
  }


  import cats.MonadError
  import cats.instances.either._

  type ErrorOr[A] = Either[String, A]
  val monadErrorEither = MonadError[ErrorOr, String]
  val success = monadErrorEither.pure(32)
  val failure = monadErrorEither.raiseError[Int]("something wronga")

  // recover
  val handleError: Either[String, Int] = monadErrorEither.handleError(failure){
    case "Badness" => 44
    case _ => 89
  }

  // recoverWith
  val handleError2: ErrorOr[Int] = monadErrorEither.handleErrorWith(failure) {
    case "Badness" => monadErrorEither.pure(44)
    case _ => Left("Something else")
  }

  // filter
  val filteredSuccess = monadErrorEither.ensure(success)("Number too small")(_ > 100)

  // Try and Future
  import cats.instances.try_._
  val exception = new RuntimeException("Really bad")
  val pureException: Try[Int] = MonadError[Try, Throwable].raiseError(exception)
  import cats.instances.future._
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))
  MonadError[Future, Throwable].raiseError(exception)

  // applicative => ApplicativeError
  import cats.data.Validated
  import cats.instances.list._
  type ErrorsOr[T] = Validated[List[String], T]

//  import cats.ApplicativeError
//  val applErrorVal = ApplicativeError[ErrorOr, List[String]]
//  // pure, raiseError, handleError, handleErrorWith
//
//  // extension methods
//  import cats.syntax.applicative._
//  import cats.syntax.applicativeError._
//
//  val extendedSuccess = 42.pure[ErrorOr]
//  val extendedError = List("Badness").raiseError[ErrorOr, Int]

  import cats.syntax.monadError._
  val testedSuccess = success.ensure("Something bad")(_ > 100)

  def main(args: Array[String]): Unit = {

  }

}
