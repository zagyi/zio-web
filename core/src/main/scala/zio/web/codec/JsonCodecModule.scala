package zio.web.codec

trait JsonCodecModule extends CodecModule { m =>
  sealed trait Json

  type Input = String

  type CodecError = String

  val codecImplementation: CodecImplementation =
    new CodecImplementation {
      def encode[A](codec: Codec[A]): A => Input                     = ???
      def decode[A](codec: Codec[A]): Input => Either[CodecError, A] = ???

      def fail(message: String): CodecError = ???
    }
}
