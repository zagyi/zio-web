package zio.web

trait CodecModule {
  type Input
  type Codec[A]

  def unitCodec: Codec[Unit]

  def encode[A](codec: Codec[A], a: A): Input
  def decode[A](codec: Codec[A], input: Input): Either[String, A]
  def zipCodec[A, B](left: Codec[A], right: Codec[B]): Codec[(A, B)]
  def transformCodec[A, B](f: A => B, g: B => A): Codec[A] => Codec[B]

  def _2[A]: Codec[(Unit, A)] => Codec[A] = transformCodec[(Unit, A), A](_._2, a => ((), a))
  def _1[A]: Codec[(A, Unit)] => Codec[A] = transformCodec[(A, Unit), A](_._1, a => (a, ()))
}
