package zio.web.docs

import scala.language.implicitConversions

sealed trait Doc {
  self =>

  def <>(that: Doc): Doc =
    Doc.Append(self, that)

  def |(that: Doc): Doc =
    Doc.Union(self, that)
}

object Doc {
  implicit def stringToDoc(str: String): Doc = p(str)

  // Constructors
  case object Empty                                          extends Doc
  final case class CodeBlock(language: String, code: String) extends Doc
  final case class Heading(content: Text, level: Int)        extends Doc
  final case class Paragraph(content: Text)                  extends Doc
  final case class OrderedList(content: Doc*)                extends Doc
  final case class UnorderedList(content: Doc)               extends Doc

  // Operators
  final case class Append(left: Doc, right: Doc) extends Doc
  final case class Union(left: Doc, right: Doc)  extends Doc

  def h1(content: Text): Doc = Heading(content, 1)

  def h2(content: Text): Doc = Heading(content, 2)

  def h3(content: Text): Doc = Heading(content, 3)

  def h4(content: Text): Doc = Heading(content, 4)

  def h5(content: Text): Doc = Heading(content, 5)

  def h6(content: Text): Doc = Heading(content, 6)

  def p(content: Text): Doc = Paragraph(content)

  def codeBlock(language: String, code: String): Doc = CodeBlock(language = language, code = code)

  def ol(content: Doc*): Doc = OrderedList(content: _*)

  def ul(content: Doc): Doc = UnorderedList(content)

  def apply(text: String): Doc = Paragraph(Text.Regular(text))

  sealed trait RenderContext

  object RenderContext {
    final case object Root  extends RenderContext
    final case object Block extends RenderContext
    final case object List  extends RenderContext
  }

  def asMarkdown(doc: Doc): String =
    MDToken.toMdString(docToMarkdownToken(doc, RenderContext.Root))

  private def docToMarkdownToken(doc: Doc, context: RenderContext): MDToken =
    doc match {
      case Heading(content, level) =>
        level match {
          case 1 =>
            MDToken.Heading1(Text.toMdToken(content, RenderContext.Block), RenderContext.Root)
          case 2 =>
            MDToken.Heading2(Text.toMdToken(content, RenderContext.Block), RenderContext.Root)
          case 3 =>
            MDToken.Heading3(Text.toMdToken(content, RenderContext.Block), RenderContext.Root)
          case 4 =>
            MDToken.Heading4(Text.toMdToken(content, RenderContext.Block), RenderContext.Root)
          case 5 =>
            MDToken.Heading5(Text.toMdToken(content, RenderContext.Block), RenderContext.Root)
          case 6 =>
            MDToken.Heading6(Text.toMdToken(content, RenderContext.Block), RenderContext.Root)
        }
      case Paragraph(content)  => MDToken.Paragraph(Text.toMdToken(content, context), context)
      case Append(left, right) => docToMarkdownToken(left, context) <> docToMarkdownToken(right, context)
      case _                   => MDToken.NotImplemented()
    }

  sealed trait Text {
    self =>

    def <>(that: Text): Text =
      Text.Append(self, that)
  }

  object Text {
    implicit def stringToInlineElement(str: String): Text = Text.Regular(str)

    final case class Regular(value: String)                 extends Text
    final case class Italic(content: Text)                  extends Text
    final case class Bold(content: Text)                    extends Text
    final case class Strikethrough(content: Text)           extends Text
    final case class Code(value: String)                    extends Text
    final case class Link(url: String, description: String) extends Text
    final case class Append(left: Text, right: Text)        extends Text

    def regular(value: String): Text     = Regular(value)
    def italic(value: Text): Text        = Italic(value)
    def bold(value: Text): Text          = Bold(value)
    def strikethrough(value: Text): Text = Strikethrough(value)

    def toMdToken(text: Text, context: RenderContext): MDToken = text match {
      case Regular(value)         => MDToken.Text(value, context)
      case Italic(content)        => MDToken.Italic(toMdToken(content, context), context)
      case Bold(content)          => MDToken.Bold(toMdToken(content, context), context)
      case Strikethrough(content) => MDToken.Strikethrough(toMdToken(content, context), context)
      case Append(left, right)    => toMdToken(left, context) <> toMdToken(right, context)
      case _                      => MDToken.NotImplemented()
    }
  }

  sealed trait MDToken { self =>

    def <>(that: MDToken): MDToken =
      MDToken.Append(self, that)
  }

  object MDToken {
    final case class Text(value: String, context: RenderContext)           extends MDToken
    final case class Italic(value: MDToken, context: RenderContext)        extends MDToken
    final case class Bold(value: MDToken, context: RenderContext)          extends MDToken
    final case class Strikethrough(value: MDToken, context: RenderContext) extends MDToken
    final case class Heading1(value: MDToken, context: RenderContext)      extends MDToken
    final case class Heading2(value: MDToken, context: RenderContext)      extends MDToken
    final case class Heading3(value: MDToken, context: RenderContext)      extends MDToken
    final case class Heading4(value: MDToken, context: RenderContext)      extends MDToken
    final case class Heading5(value: MDToken, context: RenderContext)      extends MDToken
    final case class Heading6(value: MDToken, context: RenderContext)      extends MDToken
    final case class Paragraph(content: MDToken, context: RenderContext)   extends MDToken
    final case class OrderedList(context: RenderContext)                   extends MDToken
    final case class UnorderedList(context: RenderContext)                 extends MDToken
    final case class ListItem(context: RenderContext)                      extends MDToken
    final case class NotImplemented()                                      extends MDToken

    // Operators
    final case class Append(left: MDToken, right: MDToken) extends MDToken

    def toMdString(token: MDToken): String = token match {
      case Text(value, _)          => value
      case Append(left, right)     => toMdString(left) + toMdString(right)
      case Heading1(value, _)      => s"# ${toMdString(value)}\n"
      case Heading2(value, _)      => s"## ${toMdString(value)}\n"
      case Heading3(value, _)      => s"### ${toMdString(value)}\n"
      case Heading4(value, _)      => s"#### ${toMdString(value)}\n"
      case Heading5(value, _)      => s"##### ${toMdString(value)}\n"
      case Heading6(value, _)      => s"###### ${toMdString(value)}\n"
      case Paragraph(content, _)   => s"${toMdString(content)}\n\n"
      case Italic(value, _)        => s"*${toMdString(value)}*"
      case Bold(value, _)          => s"__${toMdString(value)}__"
      case Strikethrough(value, _) => s"~~${toMdString(value)}~~"
      case _                       => s"👉 unhandled_markdown_element $token 👈"
    }
  }
}
