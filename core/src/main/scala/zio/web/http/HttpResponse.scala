package zio.web.http

sealed trait HttpResponse[+A] { self =>
  def <>[A1 >: A](that: HttpResponse[A1]): HttpResponse[A1] = self.orElse(that)

  def map[B](f: A => B): HttpResponse[B] = HttpResponse.Map(self, f)

  def orElse[A1 >: A](that: HttpResponse[A1]): HttpResponse[A1] =
    self.orElseEither(that).map(_.merge)

  def orElseEither[B](that: HttpResponse[B]): HttpResponse[Either[A, B]] = HttpResponse.OrElseEither(self, that)

  def zip[B](that: HttpResponse[B]): HttpResponse[(A, B)] = HttpResponse.Zip(self, that)

  def zipWith[B, C](that: HttpResponse[B])(f: (A, B) => C): HttpResponse[C] = self.zip(that).map(f.tupled)
}

object HttpResponse {
  case object Succeed                                                                extends HttpResponse[Unit]
  case object Fail                                                                   extends HttpResponse[Nothing]
  final case object StatusCode                                                       extends HttpResponse[Int]
  final case class Header(name: String)                                              extends HttpResponse[String]
  final case class Map[A, B](request: HttpResponse[A], f: A => B)                    extends HttpResponse[B]
  final case class OrElseEither[A, B](left: HttpResponse[A], right: HttpResponse[B]) extends HttpResponse[Either[A, B]]
  final case class Zip[A, B](left: HttpResponse[A], right: HttpResponse[B])          extends HttpResponse[(A, B)]
}
