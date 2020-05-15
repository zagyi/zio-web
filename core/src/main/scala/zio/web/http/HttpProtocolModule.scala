package zio.web.http

import zio.web._

trait HttpProtocolModule extends ProtocolModule {
  type ServerConfig  = HttpServerConfig
  type ServerService = Any
}
