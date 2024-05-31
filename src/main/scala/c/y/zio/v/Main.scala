package c.y.zio.v

import io.getquill.jdbczio.Quill
import zio.ZIOAppDefault
import zio.*
import zio.http.*

object Main extends ZIOAppDefault{

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = Server.serve(
    HellRoutes().toHttpApp ++ ApiController().toHttpApp
  ).provide(
    Server.defaultWithPort(8080),
    Quill.Mysql.fromNamingStrategy(io.getquill.SnakeCase),
    Quill.DataSource.fromPrefix("ZioMysqlAppConfig"),
    MysqlUserRepo.live
  ).debug
}
