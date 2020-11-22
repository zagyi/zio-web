package zio.web.http.internal

import java.io.StringReader
import zio.{Chunk, Task}
import zio.test.Assertion._
import zio.test._
import zio.web.http.internal.HttpLexer.HeaderParseError._
import zio.web.http.internal.HttpLexer.parseHeaders
import zio.web.http.model.{Method, Version}

import scala.util.Random

object HttpLexerSpec extends DefaultRunnableSpec {

  override def spec =
    suite("All tests")(startLineSuite, headerSuite)

  def startLineSuite = suite("HTTP start line parsing")(
    test("check OPTIONS method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("OPTIONS /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.OPTIONS))
    },
    test("check GET method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("GET /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.GET))
    },
    test("check HEAD method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("HEAD /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.HEAD))
    },
    test("check POST method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("POST /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.POST))
    },
    test("check PUT method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("PUT /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.PUT))
    },
    test("check PATCH method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("PATCH /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.PATCH))
    },
    test("check DELETE method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("DELETE /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.DELETE))
    },
    test("check TRACE method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("TRACE /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.TRACE))
    },
    test("check CONNECT method") {
      val (method, _, _) = HttpLexer.parseStartLine(new StringReader("CONNECT /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(method)(equalTo(Method.CONNECT))
    },
    test("check HTTP 1.1 version") {
      val (_, _, version) = HttpLexer.parseStartLine(new StringReader("POST /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(version)(equalTo(Version.V1_1))
    },
    test("check HTTP 2 version") {
      val (_, _, version) = HttpLexer.parseStartLine(new StringReader("POST /hello.htm HTTP/2.0\r\nheaders and body"))
      assert(version)(equalTo(Version.V2))
    },
    test("check long URI") {
      val longString = "a" * 2021
      val (_, _, version) = HttpLexer.parseStartLine(
        new StringReader(s"POST https://absolute.uri/$longString HTTP/2.0\r\nheaders and body")
      )
      assert(version)(equalTo(Version.V2))
    },
    testM("check too long URI") {
      val longString = "a" * 2028
      val result = Task(
        HttpLexer
          .parseStartLine(new StringReader(s"POST https://absolute.uri/$longString HTTP/2.0\r\nheaders and body"))
      ).run
      assertM(result)(fails(isSubtype[IllegalStateException](hasMessage(equalTo("Malformed HTTP start-line")))))
    },
    testM("check corrupted HTTP request (no space)") {
      val result = Task(HttpLexer.parseStartLine(new StringReader("POST/hello.htm HTTP/2.0\r\nheaders and body"))).run
      assertM(result)(fails(isSubtype[IllegalStateException](hasMessage(equalTo("Malformed HTTP start-line")))))
    },
    testM("check corrupted HTTP request (double CR)") {
      val result =
        Task(HttpLexer.parseStartLine(new StringReader("POST /hello.htm HTTP/2.0\r\r\nheaders and body"))).run
      assertM(result)(fails(isSubtype[IllegalStateException](hasMessage(equalTo("Malformed HTTP start-line")))))
    },
    testM("check corrupted HTTP request (random string)") {
      val result = Task(HttpLexer.parseStartLine(new StringReader(new Random().nextString(2048)))).run
      assertM(result)(fails(isSubtype[IllegalStateException](hasMessage(equalTo("Malformed HTTP start-line")))))
    },
    testM("check corrupted HTTP request (very long random string)") {
      val result = Task(HttpLexer.parseStartLine(new StringReader(new Random().nextString(4096000)))).run
      assertM(result)(fails(isSubtype[IllegalStateException](hasMessage(equalTo("Malformed HTTP start-line")))))
    },
    testM("check invalid HTTP method") {
      val result = Task(HttpLexer.parseStartLine(new StringReader("GRAB /hello.htm HTTP/2.0\r\nheaders and body"))).run
      assertM(result)(fails(isSubtype[IllegalArgumentException](hasMessage(equalTo("Unable to handle method: GRAB")))))
    },
    testM("check invalid HTTP version") {
      val result = Task(HttpLexer.parseStartLine(new StringReader("POST /hello.htm HTTP2.0\r\nheaders and body"))).run
      assertM(result)(
        fails(isSubtype[IllegalArgumentException](hasMessage(equalTo("Unable to handle version: HTTP2.0"))))
      )
    },
    testM("check empty input") {
      val result = Task(HttpLexer.parseStartLine(new StringReader(""))).run
      assertM(result)(fails(isSubtype[IllegalStateException](hasMessage(equalTo("Malformed HTTP start-line")))))
    },
    test("check URI") {
      val (_, uri, _) = HttpLexer.parseStartLine(new StringReader("OPTIONS /hello.htm HTTP/1.1\r\nheaders and body"))
      assert(uri.toString)(equalTo("/hello.htm"))
    }
  )

    final case class HeaderLines(s: String) extends AnyVal {
    def toStringWithCRLF: String = s.stripMargin.replaceAll("\n", "\r\n") + "\r\n\r\n"
  }

  private val multilineHeader =
    HeaderLines("""foo: obsolete
                  |     multiline
                  |     header""")

  val failureScenarios: Gen[Any, (String, HttpLexer.HeaderParseError)] =
    Gen.fromIterable(
      Seq(
        ""                              -> UnexpectedEnd,
        "\r"                            -> ExpectedLF(-1),
        "a"                             -> UnexpectedEnd,
        "a:"                            -> UnexpectedEnd,
        "a: "                           -> UnexpectedEnd,
        "a: b"                          -> UnexpectedEnd,
        "a: b\r"                        -> ExpectedLF(-1),
        "a: b\r\n"                      -> UnexpectedEnd,
        "a: b\r\na"                     -> UnexpectedEnd,
        "space-after-header-name : ..." -> InvalidCharacterInName(' '),
        // TODO: handling of this case could be improved, as the spec allows for
        //       multiline headers, even though that construct is deprecated
        // "A server that receives an obs-fold in a request message that is not
        //  within a message/http container MUST either reject the message by
        //  sending a 400 (Bad Request), preferably with a representation
        //  explaining that obsolete line folding is unacceptable, or replace
        //  each received obs-fold with one or more SP octets prior to
        //  interpreting the field value or forwarding the message downstream."
        // https://tools.ietf.org/html/rfc7230#section-3.2.4
        multilineHeader.toStringWithCRLF -> InvalidCharacterInName(' ')
      )
    )

  def headerSuite =
    suite("http header lexer")(
      parseHeaderTestWithRawString(
        "zero headers",
        "\r\n",
        "Host"
      )(Chunk.empty),
      //
      parseHeaderTest(
        "single header",
        HeaderLines("""Host: foo.com"""),
        "Host"
      )("foo.com"),
      //
      parseHeaderTest(
        "two headers",
        HeaderLines("""Host: foo.com
                      |Connection: Keep-Alive"""),
        "Host",
        "Connection"
      )("foo.com", "Keep-Alive"),
      //
      // TODO: replace "empty header *" by adding generator for empty/whitespace
      //       string and header to parse
      parseHeaderTest(
        "empty header 1",
        HeaderLines("Host:  \t "),
        "Host"
      )(""),
      //
      parseHeaderTest0(
        "empty header 2",
        HeaderLines("Host: \t  "),
        "foo"
      )(Chunk.empty),
      //
      parseHeaderTest0(
        "multiple headers with the same name",
        HeaderLines("""Host: foo.com
                      |foo: 1
                      |foo: 2""".stripMargin),
        "Host",
        "foo"
      )(Chunk("foo.com"), Chunk("1", "2")),
      //
      parseHeaderTest(
        "case insensitive",
        HeaderLines("""Host: foo.com
                      |Connection: Keep-Alive"""),
        "Host",
        "Connection"
      )("foo.com", "Keep-Alive"),
      //
      parseHeaderTestWithRawString(
        "header values are trimmed",
        "Host:  \t  foo.com   \t  \r\n" +
          "foo:   b  a  r    \r\n" +
          "\r\n",
        "Host",
        "foo"
      )(Chunk("foo.com"), Chunk("b  a  r")),
      //
      testM("failure scenarios") {
        checkM(failureScenarios) {
          case (request, expectedError) =>
            assertM(
              Task(
                parseHeaders(Array("some-header"), new StringReader(request))
              ).run
            )(fails(equalTo(expectedError)))
        }
      },
      //
      test("don't parse past the end of the headers") {
        val headerLines =
          HeaderLines(
            """Host: 123.com
              |foo: bar"""
          ).toStringWithCRLF

        val headerReader = new StringReader(headerLines + "body")

        val parsedHeaders =
          HttpLexer.parseHeaders(Array("Host", "foo"), headerReader)

        val remainder = {
          var c         = -1
          val remainder = new StringBuilder()
          while ({ c = headerReader.read(); c != -1 }) remainder.append(c.toChar)
          remainder.toString
        }

        assert(parsedHeaders.toSeq)(
          equalTo(Seq(Chunk("123.com"), Chunk("bar")))
        ) &&
        assert(remainder)(equalTo("body"))
      }
    )

  def parseHeaderTest(name: String, headerLines: HeaderLines, firstHeader: String, otherHeaders: String*)(
    firstExpectedValue: String,
    otherExpectedValues: String*
  ): ZSpec[Any, Nothing] = {
    val expectedValues = (firstExpectedValue +: otherExpectedValues).map(Chunk(_))
    parseHeaderTest0(name, headerLines, firstHeader, otherHeaders: _*)(expectedValues: _*)
  }

  def parseHeaderTest0(name: String, headerLines: HeaderLines, firstHeader: String, otherHeaders: String*)(
    expectedValues: Chunk[String]*
  ): ZSpec[Any, Nothing] =
    parseHeaderTestWithRawString(name, headerLines.toStringWithCRLF, firstHeader, otherHeaders: _*)(expectedValues: _*)

  def parseHeaderTestWithRawString(name: String, headerLines: String, firstHeader: String, otherHeaders: String*)(
    expectedValues: Chunk[String]*
  ): ZSpec[Any, Nothing] =
    test(name) {
      val headers                    = (firstHeader +: otherHeaders).toArray
      val actual: Seq[Chunk[String]] = parseHeaders(headers, new StringReader(headerLines)).toSeq
      assert(actual)(equalTo(expectedValues))
    }
}
