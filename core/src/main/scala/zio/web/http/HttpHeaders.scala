package zio.web.http

final case class HttpHeaders(value: Map[String, String]) { self =>

  def ++(that: HttpHeaders): HttpHeaders =
    if (self eq HttpHeaders.empty) that
    else if (that eq HttpHeaders.empty) self
    else HttpHeaders(self.value ++ that.value)
}

object HttpHeaders {
  val empty: HttpHeaders = HttpHeaders(Map())
}
