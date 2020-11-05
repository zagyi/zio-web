package zio.web.codec

import zio.stream.ZTransducer

trait JsonCodecModule extends CodecModule {
  // TODO: Implement for ZIO JSON
  type Input = String

  type CodecError = String

  val codecImplementation: CodecImplementation =
    new CodecImplementation {
      def encoder[A](codec: Codec[A]): ZTransducer[Any, Nothing, A, Input]    = ???
      def decoder[A](codec: Codec[A]): ZTransducer[Any, CodecError, Input, A] = ???

      def fail(message: String): CodecError = ???
    }
}
