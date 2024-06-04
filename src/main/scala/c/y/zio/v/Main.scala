package c.y.zio.v

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZIOAppDefault
import zio.*
import zio.http.*
import zio.logging.backend.SLF4J
import zio.logging.LogFormat

object Main extends ZIOAppDefault{

  override val bootstrap = SLF4J.slf4j(LogLevel.Info, LogFormat.colored)

  val apps = HellRoutes() ++ ApiController()
  val wrapped = apps @@ Middleware.debug @@ CustomMiddleware.correlationId //Middleware.addHeader("XYZ", "123")

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = Server.serve(
    wrapped.toHttpApp
  ).provide(
    Server.defaultWithPort(8080),
//    Quill.Mysql.fromNamingStrategy(io.getquill.SnakeCase),
    Quill.Mysql.fromNamingStrategy(SnakeCase),
    Quill.DataSource.fromPrefix("ZioMysqlAppConfig"),
    MysqlUserRepo.live
  ).debug
}
