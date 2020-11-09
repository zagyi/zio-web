package zio.web

import java.io.IOException

import _root_.zio.{ Has, ZLayer }

trait ProtocolModule extends EndpointModule {
  type ServerConfig
  type ClientConfig
  type ServerService
  type ProtocolDocs
  type Middleware[-R, +E]
  type MinMetadata
  type MaxMetadata

  def makeServer[M >: MaxMetadata <: MinMetadata, R <: Has[ServerConfig], E, A](
    middleware: Middleware[R, E],
    endpoints: Endpoints[M, A]
  ): ZLayer[R, IOException, Has[ServerService]]

  def makeDocs[M >: MaxMetadata <: MinMetadata](endpoints: Endpoints[M, _]): ProtocolDocs

  def makeClient[M >: MaxMetadata <: MinMetadata, A](
    endpoints: Endpoints[M, A]
  ): ZLayer[Has[ClientConfig], IOException, Has[ClientService[A]]]
}
