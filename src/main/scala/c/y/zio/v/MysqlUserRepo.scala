package c.y.zio.v

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID
import javax.sql.DataSource

case class UserTable(uuid: UUID, name: String, age: Int)

case class MysqlUserRepo(quill: Quill.Mysql[SnakeCase]) extends UserRepo {
  import quill.*

  override def register(user: User): Task[String] = {
    for {
      id <- Random.nextUUID
      _ <- run {
        quote {
          query[UserTable].insertValue(
            lift(UserTable(id, user.name, user.age))
          )
        }
      }
    } yield id.toString
  }

  override def lookup(id: String): Task[Option[User]] =
    run {
      quote{
        query[UserTable]
          .filter(p => p.uuid == lift(UUID.fromString(id)))
          .map(u => User(u.name, u.age))
      }
  }.map(_.headOption)

  override def usesr: Task[List[User]] =
    run {
      quote {
        query[UserTable].map(u => User(u.name, u.age))
      }
    }
}

object MysqlUserRepo {
  val live: ZLayer[Quill.Mysql[SnakeCase], Nothing, MysqlUserRepo] =
    ZLayer.fromFunction(new MysqlUserRepo(_))
}