package zio.web

import java.io.IOException

import zio._

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
    service: Service[M, A]
  ): ZLayer[R, IOException, Has[ServerService]]

  def makeDocs[M >: MaxMetadata <: MinMetadata](service: Service[M, _]): ProtocolDocs

  def makeClient[M >: MaxMetadata <: MinMetadata, A](
    service: Service[M, A]
  ): ZLayer[Has[ClientConfig], IOException, Has[ClientService[A]]]
}
