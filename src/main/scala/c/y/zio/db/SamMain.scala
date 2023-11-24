package c.y.zio.db

import io.getquill._
import io.getquill.jdbczio.Quill
import zio._

import java.sql.SQLException


case class Employee(pid: Int, name: String, age: Int)

class DataService(quill: Quill.Mysql[SnakeCase]) {//Postgres[SnakeCase]) {
  import quill._
  def getPeople: ZIO[Any, SQLException, List[Employee]] = run(query[Employee])

  def insertPeople(name: String, age: Int): ZIO[Any, SQLException, Int] = {
    val q = quote {
      query[Employee].insertValue(lift(Employee(0, name, age))).returningGenerated(r => r.pid)
    }
    run(q)
  }

}
object DataService {
  def getPeople: ZIO[DataService, SQLException, List[Employee]] =
    ZIO.serviceWithZIO[DataService](_.getPeople)

  def insertPeople(name: String, age: Int) = ZIO.serviceWithZIO[DataService](_.insertPeople(name, age))

  val live = ZLayer.fromFunction(new DataService(_))
}

object Main extends ZIOAppDefault {

  val program = for {
    _ <- DataService.getPeople.debug
    res <- DataService.insertPeople("Jane", 17).debug
    _ <- DataService.getPeople.debug
  } yield ()

  override def run = {
//    DataService.getPeople
    println(program)
    program
      .provide(
        DataService.live,
        Quill.Mysql.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("ZioMysqlAppConfig")
      )
      .debug("Results")
      .exitCode
  }
}
