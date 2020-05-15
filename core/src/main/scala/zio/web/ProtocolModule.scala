package zio.web

import java.io.IOException

import zio._

trait ProtocolModule extends EndpointModule {
  type ServerConfig
  type ServerService
  type ProtocolDocs

  def server(service: Service): ZLayer[ServerConfig, IOException, Has[ServerService]]

  def docs(service: Service): ProtocolDocs

  def client: ZLayer[WebConfig, IOException, Has[ClientService]]
}
