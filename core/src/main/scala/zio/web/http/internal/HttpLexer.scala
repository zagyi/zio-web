package zio.web.http.internal

import zio.web.http.model.{ Method, Uri, Version }

import scala.collection.mutable.Queue

/*
GET /hello.htm HTTP/1.1
User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)
Host: www.tutorialspoint.com
Accept: text/xml
Content-Type: text/xml; charset=utf-8
Content-Length: length
Accept-Language: en-us
Accept-Encoding: gzip, deflate
Connection: Keep-Alive

<?xml version="1.0" encoding="utf-8"?>
<string xmlns="http://clearforest.com/">string</string>
 */
object HttpLexer {

  /**
   *
   * Parses start-line and returns a tuple of {@link zio.web.http.model.Method}, {@link zio.web.http.model.Uri} and {@link zio.web.http.model.Version}
   *
   * @param reader - HTTP request
   * @param methodLimit - defines maximum HTTP method length
   * @param uriLimit - defines maximum HTTP URI length (2048 search engine friendly)
   * @param versionLimit - defines maximum HTTP version length (according to the spec and available HTTP versions it can be 8)
   * @return a tuple of Method, Uri and Version
   */
  def parseStartLine(
    reader: java.io.Reader,
    methodLimit: Int = 7,
    uriLimit: Int = 2048,
    versionLimit: Int = 8
  ): (Method, Uri, Version) = {
    //TODO: not sure that it actually supports HTTP 2, I just started digging into HTTP 2 and it looks like a different story
    // it uses something called frames and has a different layout
    //TODO: https://undertow.io/blog/2015/04/27/An-in-depth-overview-of-HTTP2.html
    //TODO: https://developers.google.com/web/fundamentals/performance/http2/

    require(reader != null)
    require(reader.ready())

    // end of a line is CRLF
    val CR = 0x0D
    val LF = 0x0A
    val SP = 0x20 // elements separated by space

    val elements       = Queue[String]()
    var currentElement = new StringBuilder
    var char           = reader.read()

    //there is no need in reading the whole http request, so reading till the end of the first line
    while (char != LF) {

      // define and check the corresponding limit based on a currently processing element
      checkCurrentElementSize(currentElement.size, elements.size match {
        case 0 => methodLimit
        case 1 => uriLimit
        case 2 => versionLimit
      })

      char match {
        case c if c == SP || (c == CR && elements.size == 2) =>
          elements += currentElement.toString(); currentElement = new StringBuilder
        case _ => currentElement.append(char.toChar)
      }

      char = reader.read()

      if (elements.size == 3 && char != LF)
        throw new IllegalStateException("Malformed HTTP start-line")
    }

    def checkCurrentElementSize(elementSize: Int, limit: Int): Unit =
      if (elementSize > limit) throw new IllegalStateException("Malformed HTTP start-line")

    (Method.fromString(elements.dequeue()), Uri.fromString(elements.dequeue()), Version.fromString(elements.dequeue()))
  }

  /**
   * Parses the headers with the specified names, returning an array that contains header values
   * for the headers with those names. Returns null strings if it couldn't find the headers before
   * the end of the headers.
   */
  def parseHeaders(headers: Array[String], reader: java.io.Reader): Array[String] = {
    // TODO:
    // 1. Don't parse past the end of the headers (\r\n\r\n)
    // 2. Correctly parse newlines (\r\n)
    // 3. Skip past ':' after header name
    // 4. Skip past whitespace after (or before?) colon ':'
    // 5. Be case-insensitive
    // 6. Handle duplicate headers???
    // 7. Async???
    val output = Array.ofDim[String](headers.length)

    var matrix: StringMatrix = new StringMatrix(headers)
    var bitset: Long         = 0L
    var i: Int               = 0

    var c: Int = -1
    while ({ c = reader.read(); c != -1 }) {
      if (c == '\n') {
        matrix = new StringMatrix(headers)
        bitset = 0L
        i = 0
      }
      bitset = matrix.update(bitset, i, c)
      i += 1

      bitset = matrix.exact(bitset, i)
      val first = matrix.first(bitset)

      if (first != -1) {
        val sb = new StringBuilder()
        // Content-Type:
        while ({ c = reader.read(); c != -1 && c != '\n' }) {
          sb.append(c)
        }
        output(first) = sb.toString()
      }
    }

    output
  }

  // Adapted from ZIO JSON, credits to Sam Halliday (@fommil).
  final private class StringMatrix(val xs: Array[String]) {
    require(xs.forall(_.nonEmpty))
    require(xs.nonEmpty)
    require(xs.length < 64)

    val width               = xs.length
    val height: Int         = xs.map(_.length).max
    val lengths: Array[Int] = xs.map(_.length)
    val initial: Long       = (0 until width).foldLeft(0L)((bs, r) => bs | (1L << r))
    private val matrix: Array[Int] = {
      val m           = Array.fill[Int](width * height)(-1)
      var string: Int = 0
      while (string < width) {
        val s         = xs(string)
        val len       = s.length
        var char: Int = 0
        while (char < len) {
          m(width * char + string) = s.codePointAt(char)
          char += 1
        }
        string += 1
      }
      m
    }

    // must be called with increasing `char` (starting with bitset obtained from a
    // call to 'initial', char = 0)
    def update(bitset: Long, char: Int, c: Int): Long =
      if (char >= height) 0L    // too long
      else if (bitset == 0L) 0L // everybody lost
      else {
        var latest: Long = bitset
        val base: Int    = width * char

        if (bitset == initial) { // special case when it is dense since it is simple
          var string: Int = 0
          while (string < width) {
            if (matrix(base + string) != c)
              latest = latest ^ (1L << string)
            string += 1
          }
        } else {
          var remaining: Long = bitset
          while (remaining != 0L) {
            val string: Int = java.lang.Long.numberOfTrailingZeros(remaining)
            val bit: Long   = 1L << string
            if (matrix(base + string) != c)
              latest = latest ^ bit
            remaining = remaining ^ bit
          }
        }

        latest
      }

    // excludes entries that are not the given exact length
    def exact(bitset: Long, length: Int): Long =
      if (length > height) 0L // too long
      else {
        var latest: Long    = bitset
        var remaining: Long = bitset
        while (remaining != 0L) {
          val string: Int = java.lang.Long.numberOfTrailingZeros(remaining)
          val bit: Long   = 1L << string
          if (lengths(string) != length)
            latest = latest ^ bit
          remaining = remaining ^ bit
        }
        latest
      }

    def first(bitset: Long): Int =
      if (bitset == 0L) -1
      else java.lang.Long.numberOfTrailingZeros(bitset) // never returns 64
  }
}
