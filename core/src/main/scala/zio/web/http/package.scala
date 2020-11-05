package zio.web

package object http extends HttpProtocolModule {
  val defaultProtocol: codec.Protocol = codec.JsonProtocol
}
