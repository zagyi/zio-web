package zio.web.codec

sealed trait StandardType[A]

object StandardType {
  implicit object UnitCodec   extends StandardType[Unit]
  implicit object StringCodec extends StandardType[String]
  implicit object BoolCodec   extends StandardType[Boolean]
  implicit object ShortCodec  extends StandardType[Short]
  implicit object IntCodec    extends StandardType[Int]
  implicit object LongCodec   extends StandardType[Long]
  implicit object FloatCodec  extends StandardType[Float]
  implicit object DoubleCodec extends StandardType[Double]
  implicit object ByteCodec   extends StandardType[Byte]
  implicit object CharCodec   extends StandardType[Char]

  // TODO: Add java.time
}
