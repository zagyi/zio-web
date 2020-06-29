package zio.web

import java.io.IOException

import zio._

trait ProtocolModule extends EndpointModule {
  type ServerConfig
  type ClientConfig
  type ServerService
  type ProtocolDocs
  type Middleware[-R, +E]

  def makeServer[R <: Has[ServerConfig], E](
    middleware: Middleware[R, E],
    service: Service
  ): ZLayer[R, IOException, Has[ServerService]]

  def makeDocs(service: Service): ProtocolDocs

  def makeClient: ZLayer[Has[ClientConfig], IOException, Has[ClientService]]
}
