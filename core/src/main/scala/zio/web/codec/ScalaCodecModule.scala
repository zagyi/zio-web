package zio.web.codec

trait ScalaCodecModule extends CodecModule {
  def stringCodec: Codec[String]
  def boolCodec: Codec[Boolean]
  def shortCodec: Codec[Short]
  def intCodec: Codec[Int]
  def longCodec: Codec[Long]
  def floatCodec: Codec[Float]
  def doubleCodec: Codec[Double]
  def byteCodec: Codec[Byte]
  def charCodec: Codec[Char]
  // def sequenceCodec[A](codec: Codec[A]): Codec[Chunk[A]]
}
