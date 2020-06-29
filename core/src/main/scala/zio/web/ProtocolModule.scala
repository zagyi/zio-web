package zio.web

import java.io.IOException

import zio._

trait ProtocolModule extends EndpointModule {
  type ServerConfig
  type ClientConfig
  type ServerService
  type ProtocolDocs

  def makeServer(service: Service): ZLayer[Has[ServerConfig], IOException, Has[ServerService]]

  def makeDocs(service: Service): ProtocolDocs

  def makeClient: ZLayer[Has[ClientConfig], IOException, Has[ClientService]]
}
