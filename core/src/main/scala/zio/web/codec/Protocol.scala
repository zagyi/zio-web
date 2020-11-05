package zio.web.codec

import zio.stream.ZTransducer

trait Protocol {
  def encoder[A](codec: Codec[A]): ZTransducer[Any, Nothing, A, Byte]
  def decoder[A](codec: Codec[A]): ZTransducer[Any, String, Byte, A]
}
