package c.y.zio.v

import zio.*

case class InMemRepo(map: Ref[Map[String, User]]) extends UserRepo:
  override def register(user: User): Task[String] = for{
    id <- Random.nextUUID.map(_.toString)
    _ <- map.update(_ + (id -> user))
  } yield id

  override def lookup(id: String): Task[Option[User]] =
    map.get.map(_.get(id))

  override def usesr: Task[List[User]] =
    map.get.map(_.values.toList)

object InMemRepo {
  def layer: ZLayer[Any, Nothing, InMemRepo] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[String, User]).map(InMemRepo(_))
    )
}
