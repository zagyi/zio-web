package zio.web.http

import zio.ZIO

final case class HttpMiddleware[-R, +E](
  request: HttpMiddleware.Request[R, E],
  response: HttpMiddleware.Response[R, E]
) { self =>

  def <>[R1 <: R, E1 >: E](that: HttpMiddleware[R1, E1]): HttpMiddleware[R1, E1] =
    HttpMiddleware(self.request <> that.request, self.response <> that.response)
}

object HttpMiddleware {
  val none: HttpMiddleware[Any, Nothing] = HttpMiddleware(Request.none, Response.none)

  trait Request[-R, +E] { self =>
    type Metadata

    val pattern: HttpRequest[Metadata]

    val processor: Metadata => ZIO[R, E, Unit]

    def <>[R1 <: R, E1 >: E](that: Request[R1, E1]): Request[R1, E1] =
      new Request[R1, E1] {
        type Metadata = (self.Metadata, that.Metadata)

        val pattern = self.pattern.zip(that.pattern)

        val processor = (metadata: Metadata) => self.processor(metadata._1) *> that.processor(metadata._2)
      }
  }

  object Request {

    def apply[R, E, M](p: HttpRequest[M], f: M => ZIO[R, E, Unit]): Request[R, E] =
      new Request[R, E] {
        type Metadata = M
        val pattern   = p
        val processor = f
      }

    val none: Request[Any, Nothing] = apply[Any, Nothing, Unit](HttpRequest.Succeed, _ => ZIO.unit)
  }

  trait Response[-R, +E] { self =>
    type Metadata

    val pattern: HttpResponse[Metadata]

    val processor: Metadata => ZIO[R, E, Unit]

    def <>[R1 <: R, E1 >: E](that: Response[R1, E1]): Response[R1, E1] =
      new Response[R1, E1] {
        type Metadata = (self.Metadata, that.Metadata)

        val pattern = self.pattern.zip(that.pattern)

        val processor = (metadata: Metadata) => self.processor(metadata._1) *> that.processor(metadata._2)
      }
  }

  object Response {

    def apply[R, E, M](p: HttpResponse[M], f: M => ZIO[R, E, Unit]): Response[R, E] =
      new Response[R, E] {
        type Metadata = M
        val pattern   = p
        val processor = f
      }

    val none: Response[Any, Nothing] = apply[Any, Nothing, Unit](HttpResponse.Succeed, _ => ZIO.unit)
  }
}
