package zio.web.http

import zio.web._

trait HttpProtocolModule extends ProtocolModule {
  type ServerConfig       = HttpServerConfig
  type ClientConfig       = HttpClientConfig
  type ServerService      = Any
  type Middleware[-R, +E] = HttpMiddleware[R, E]
  type MinMetadata        = Any
  type MaxMetadata        = Route

  sealed case class Route(url: String)
}
