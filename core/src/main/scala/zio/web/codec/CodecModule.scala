package zio.web.codec

import zio.Chunk
import zio.stream.ZTransducer

trait CodecModule {
  type Input
  type CodecError

  sealed trait Codec[A] { self =>
    def ? : Codec[Option[A]] = Codec.Optional(self)

    def encoder: ZTransducer[Any, Nothing, A, Input] =
      codecImplementation.encoder(self)

    def decoder: ZTransducer[Any, CodecError, Input, A] =
      codecImplementation.decoder(self)

    def transform[B](f: A => B, g: B => A): Codec[B] =
      Codec.Transform[A, B](self, a => Right(f(a)), b => Right(g(b)))

    def transformOrFail[B](f: A => Either[CodecError, B], g: B => Either[CodecError, A]): Codec[B] =
      Codec.Transform[A, B](self, f, g)

    def zip[B](that: Codec[B]): Codec[(A, B)] = Codec.Tuple(self, that)
  }

  object Codec {
    sealed case class Record(structure: Map[String, Codec[_]])      extends Codec[Map[String, _]]
    sealed case class Sequence[A](element: Codec[A])                extends Codec[Chunk[A]]
    sealed case class Enumeration(structure: Map[String, Codec[_]]) extends Codec[Map[String, _]]
    sealed case class Transform[A, B](codec: Codec[A], f: A => Either[CodecError, B], g: B => Either[CodecError, A])
        extends Codec[B]
    sealed case class Primitive[A](standardType: StandardType[A])  extends Codec[A]
    sealed case class Tuple[A, B](left: Codec[A], right: Codec[B]) extends Codec[(A, B)]
    sealed case class Optional[A](codec: Codec[A])                 extends Codec[Option[A]]

    def apply[A](implicit codec: Codec[A]): Codec[A] = codec

    def caseClassN[A, Z](t1: (String, Codec[A]))(f: A => Z, g: Z => Option[A]): Codec[Z] =
      Codec
        .record(Map(t1))
        .transformOrFail(
          { map =>
            val v1 = map(t1._1).asInstanceOf[A]

            Right(f(v1))
          }, { (z: Z) =>
            g(z).map { a =>
              Map(t1._1 -> a)
            }.toRight(codecImplementation.fail("Cannot deconstruct case class"))
          }
        )

    def caseClassN[A, B, Z](
      t1: (String, Codec[A]),
      t2: (String, Codec[B])
    )(f: (A, B) => Z, g: Z => Option[(A, B)]): Codec[Z] =
      Codec
        .record(Map[String, Codec[_]](t1, t2))
        .transformOrFail(
          { map =>
            val v1 = map(t1._1).asInstanceOf[A]
            val v2 = map(t2._1).asInstanceOf[B]

            Right(f(v1, v2))
          }, { (z: Z) =>
            g(z).map { case (a, b) => Map(t1._1 -> a, t2._1 -> b) }
              .toRight(codecImplementation.fail("Cannot deconstruct case class"))
          }
        )

    def caseClassN[A, B, C, Z](
      t1: (String, Codec[A]),
      t2: (String, Codec[B]),
      t3: (String, Codec[C])
    )(f: (A, B, C) => Z, g: Z => Option[(A, B, C)]): Codec[Z] =
      Codec
        .record(Map[String, Codec[_]](t1, t2, t3))
        .transformOrFail(
          { map =>
            val v1 = map(t1._1).asInstanceOf[A]
            val v2 = map(t2._1).asInstanceOf[B]
            val v3 = map(t3._1).asInstanceOf[C]

            Right(f(v1, v2, v3))
          }, { (z: Z) =>
            g(z).map { case (a, b, c) => Map(t1._1 -> a, t2._1 -> b, t3._1 -> c) }
              .toRight(codecImplementation.fail("Cannot deconstruct case class"))
          }
        )

    def either[A, B](left: Codec[A], right: Codec[B]): Codec[Either[A, B]] =
      enumeration(Map("Left" -> left, "Right" -> right)).transformOrFail(
        { map =>
          map.headOption.map {
            case ("Left", v)  => Right(Left(v.asInstanceOf[A]))
            case ("Right", v) => Right(Right(v.asInstanceOf[B]))
            case _            => Left(codecImplementation.fail("Expected left or right of sum"))
          }.getOrElse(Left(codecImplementation.fail("Expected left or right of sum")))
        }, {
          case Left(v)  => Right(Map("Left"  -> v))
          case Right(v) => Right(Map("Right" -> v))
        }
      )

    def enumeration(structure: Map[String, Codec[_]]): Codec[Map[String, _]] =
      Enumeration(structure)

    def first[A](codec: Codec[(A, Unit)]): Codec[A] =
      codec.transform[A](_._1, a => (a, ()))

    implicit def list[A](implicit element: Codec[A]): Codec[List[A]] =
      sequence(element).transform(_.toList, Chunk.fromIterable(_))

    implicit def option[A](implicit element: Codec[A]): Codec[Option[A]] =
      Optional(element)

    implicit def primitive[A](implicit standardType: StandardType[A]): Codec[A] =
      Primitive(standardType)

    def record(structure: Map[String, Codec[_]]): Codec[Map[String, _]] =
      Record(structure)

    implicit def sequence[A](implicit element: Codec[A]): Codec[Chunk[A]] =
      Sequence(element)

    implicit def set[A](implicit element: Codec[A]): Codec[Set[A]] =
      sequence(element).transform(_.toSet, Chunk.fromIterable(_))

    def second[A](codec: Codec[(Unit, A)]): Codec[A] =
      codec.transform[A](_._2, a => ((), a))

    implicit def vector[A](implicit element: Codec[A]): Codec[Vector[A]] =
      sequence(element).transform(_.toVector, Chunk.fromIterable(_))

    implicit def zipN[A, B](implicit c1: Codec[A], c2: Codec[B]): Codec[(A, B)] =
      c1.zip(c2)

    implicit def zipN[A, B, C](implicit c1: Codec[A], c2: Codec[B], c3: Codec[C]): Codec[(A, B, C)] =
      c1.zip(c2).zip(c3).transform({ case ((a, b), c) => (a, b, c) }, { case (a, b, c) => ((a, b), c) })

    implicit def zipN[A, B, C, D](
      implicit c1: Codec[A],
      c2: Codec[B],
      c3: Codec[C],
      c4: Codec[D]
    ): Codec[(A, B, C, D)] =
      c1.zip(c2)
        .zip(c3)
        .zip(c4)
        .transform({ case (((a, b), c), d) => (a, b, c, d) }, { case (a, b, c, d) => (((a, b), c), d) })
  }

  val codecImplementation: CodecImplementation

  trait CodecImplementation {
    def encoder[A](codec: Codec[A]): ZTransducer[Any, Nothing, A, Input]
    def decoder[A](codec: Codec[A]): ZTransducer[Any, CodecError, Input, A]

    def fail(message: String): CodecError
  }
}
