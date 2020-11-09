package zio.web

package object http extends HttpProtocolModule {
  val defaultProtocol: codec.Codec = codec.JsonCodec

  val allProtocols: Map[String, codec.Codec] = Map("application/json" -> codec.JsonCodec)
}
