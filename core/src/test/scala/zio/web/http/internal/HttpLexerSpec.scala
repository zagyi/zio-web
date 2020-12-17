package zio.web.http.internal

import java.io.StringReader

import zio.Task
import zio.test.Assertion._
import zio.test._
import zio.web.http.model.{ Method, Version }

import scala.util.Random

object HttpLexerSpec extends DefaultRunnableSpec {

  def spec = suite("HTTP start line parsing")(
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
}
