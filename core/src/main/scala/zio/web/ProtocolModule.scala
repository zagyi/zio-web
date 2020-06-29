package zio.web

import java.io.IOException

import zio._

trait ProtocolModule extends EndpointModule {
  type ServerConfig
  type ClientConfig
  type ServerService
  type ProtocolDocs
  type Middleware[-R, +E]

  def makeServer[R <: Has[ServerConfig], E, A](
    middleware: Middleware[R, E],
    service: Service[A],
    handlers: Handlers[R, A]
  ): ZLayer[R, IOException, Has[ServerService]]

  def makeDocs(service: Service[_]): ProtocolDocs

  def makeClient[A](service: Service[A]): ZLayer[Has[ClientConfig], IOException, Has[ClientService[A]]]
}
