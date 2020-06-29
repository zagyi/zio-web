package zio.web.codec

sealed trait Semantic[Underlying, Type]

object Semantic {
  case object Date     extends Semantic[String, java.time.LocalDate]
  case object DateTime extends Semantic[String, java.time.LocalDateTime]
}
