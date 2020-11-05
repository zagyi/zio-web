package zio.web.docs

sealed trait Doc { self =>
  def <>(that: Doc): Doc = Doc.Append(self, that)
}

object Doc {  
  case object Empty                              extends Doc
  final case class Text(value: String)           extends Doc
  final case class Append(left: Doc, right: Doc) extends Doc

  def apply(text: String): Doc = Text(text)
}
