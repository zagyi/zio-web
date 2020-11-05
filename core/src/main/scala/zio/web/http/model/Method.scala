package zio.web.http.model

sealed trait Method

object Method {
  object GET    extends Method
  object PUT    extends Method
  object DELETE extends Method
  object POST   extends Method
}
