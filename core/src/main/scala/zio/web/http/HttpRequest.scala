package zio.web.http

sealed trait HttpRequest[+A] { self =>
  def <>[A1 >: A](that: HttpRequest[A1]): HttpRequest[A1] = self.orElse(that)

  def map[B](f: A => B): HttpRequest[B] = HttpRequest.Map(self, f)

  def orElse[A1 >: A](that: HttpRequest[A1]): HttpRequest[A1] =
    self.orElseEither(that).map(_.merge)

  def orElseEither[B](that: HttpRequest[B]): HttpRequest[Either[A, B]] = HttpRequest.OrElseEither(self, that)

  def zip[B](that: HttpRequest[B]): HttpRequest[(A, B)] = HttpRequest.Zip(self, that)

  def zipWith[B, C](that: HttpRequest[B])(f: (A, B) => C): HttpRequest[C] = self.zip(that).map(f.tupled)
}

object HttpRequest {
  sealed trait Scheme

  object Scheme {
    case object Any                         extends Scheme
    final case class Specific(name: String) extends Scheme
  }
  sealed trait SchemeSpecific
  sealed trait Fragment

  case object Succeed                                                              extends HttpRequest[Unit]
  case object Fail                                                                 extends HttpRequest[Nothing]
  case object Method                                                               extends HttpRequest[String]
  final case class Header(name: String)                                            extends HttpRequest[String]
  final case object URI                                                            extends HttpRequest[java.net.URI]
  final case class Map[A, B](request: HttpRequest[A], f: A => B)                   extends HttpRequest[B]
  final case class OrElseEither[A, B](left: HttpRequest[A], right: HttpRequest[B]) extends HttpRequest[Either[A, B]]
  final case class Zip[A, B](left: HttpRequest[A], right: HttpRequest[B])          extends HttpRequest[(A, B)]
}
