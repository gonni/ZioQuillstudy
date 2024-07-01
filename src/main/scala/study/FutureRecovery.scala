package study

import java.util.concurrent.Executors
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object FutureRecovery {
  protected implicit def executor: ExecutionContext =
    scala.concurrent.ExecutionContext.fromExecutor(Executors.newFixedThreadPool(6)) ///.Implicits.global

  class ExCustom(msg: String) extends Exception(msg)

  implicit class ErrorMessageFuture[A](val future: Future[A]) extends AnyVal {
    def errorMsg(error: String): Future[A] = future.recoverWith {
      case t: Throwable => Future.failed(new Exception(error, t))
    }
  }

  def callFunction1(name: =>String): Future[String] = {
    for {
      _ <- Future(println("Run AA"))
      r <- Future("[" + name + " by CF1]")
    } yield r
  }

  def callFunction2(name: => String): Future[String] = {
//    Future("[" + name + " by CF2]")
    Future.failed(new ExCustom("Custom Error"))
  }
  def callFunction3(name: => String): Future[String] = {
    Future("[" + name + " by CF3]")
  }

  def main(args: Array[String]): Unit = {
    val res = for{
      cf1 <- callFunction1("aa") errorMsg "Error aa"
      cf2 <- callFunction2(cf1) errorMsg "Error bb"
//      cf2 <- callFunction2(cf1).recoverWith{
//        case exc: ExCustom =>
//          println("Failed .. stage: 2")
//          Future.failed(new Exception(exc.getMessage))
//        case t: Throwable =>
//          println("Failed in ROOT")
//          Future.failed(new Exception("FAILED in ROOTs"))
//      }
      cf3 <- callFunction3(cf2) errorMsg "Error cc"
    } yield cf3

    res.recover(e => {
      e.printStackTrace()
      "New Future Result"
    }).foreach(m => {
      println("1. result unit =>" + m)
    })

    res.foreach(m => {
      println("2. result unit =>" + m)
    })
  }

}
