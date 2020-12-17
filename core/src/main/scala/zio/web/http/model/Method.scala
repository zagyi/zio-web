package zio.web.http.model

sealed trait Method

object Method {
  object OPTIONS extends Method
  object GET     extends Method
  object HEAD    extends Method
  object POST    extends Method
  object PUT     extends Method
  object PATCH   extends Method
  object DELETE  extends Method
  object TRACE   extends Method
  object CONNECT extends Method

  def fromString(method: String): Method =
    method match {
      case "OPTIONS" => Method.OPTIONS
      case "GET"     => Method.GET
      case "HEAD"    => Method.HEAD
      case "POST"    => Method.POST
      case "PUT"     => Method.PUT
      case "PATCH"   => Method.PATCH
      case "DELETE"  => Method.DELETE
      case "TRACE"   => Method.TRACE
      case "CONNECT" => Method.CONNECT
      case _         => throw new IllegalArgumentException(s"Unable to handle method: $method")
    }
}
