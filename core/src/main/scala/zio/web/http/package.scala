package zio.web

package object http extends HttpProtocolModule {
  val defaultProtocol: codec.Protocol = codec.JsonProtocol

  val allProtocols: Map[String, codec.Protocol] = Map("application/json" -> defaultProtocol)
}
