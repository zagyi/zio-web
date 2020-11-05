package zio.web.codec

import zio.stream.ZTransducer

object JsonProtocol extends Protocol {
  // TODO: Implement for ZIO JSON
  override def encoder[A](codec: Codec[A]): ZTransducer[Any, Nothing, A, Byte] = ???
  override def decoder[A](codec: Codec[A]): ZTransducer[Any, String, Byte, A]  = ???
}
