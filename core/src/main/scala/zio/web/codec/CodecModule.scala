package zio.web.codec

trait CodecModule {
  type Input
  type Codec[A]
  type CodecError

  def unitCodec: Codec[Unit]

  def ascribe[A, B](codec: Codec[A], semantic: Semantic[A, B]): Codec[B]
  def encode[A](codec: Codec[A], a: A): Input
  def decode[A](codec: Codec[A], input: Input): Either[String, A]
  def zipCodec[A, B](left: Codec[A], right: Codec[B]): Codec[(A, B)]

  def transformCodec[A, B](f: A => B, g: B => A): Codec[A] => Codec[B] =
    transformCodecError(a => Right(f(a)), b => Right(g(b)))
  def transformCodecError[A, B](f: A => Either[CodecError, B], g: B => Either[CodecError, A]): Codec[A] => Codec[B]

  final def _2[A]: Codec[(Unit, A)] => Codec[A] = transformCodec[(Unit, A), A](_._2, a => ((), a))
  final def _1[A]: Codec[(A, Unit)] => Codec[A] = transformCodec[(A, Unit), A](_._1, a => (a, ()))
}
