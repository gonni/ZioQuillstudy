package c.y.zio.db

import io.getquill._
import io.getquill.jdbczio.Quill
import zio._

import java.sql.SQLException

case class Person(name: String, age: Int)

class DataService(quill: Quill.Mysql[SnakeCase]) {//Postgres[SnakeCase]) {
  import quill._
  def getPeople: ZIO[Any, SQLException, List[Person]] = run(query[Person])
}
object DataService {
  def getPeople: ZIO[DataService, SQLException, List[Person]] =
    ZIO.serviceWithZIO[DataService](_.getPeople)

  val live = ZLayer.fromFunction(new DataService(_))
}
object Main extends ZIOAppDefault {
  override def run = {
    DataService.getPeople
      .provide(
        DataService.live,
        Quill.Mysql.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("ZioMysqlAppConfig")
      )
      .debug("Results")
      .exitCode
  }
}
