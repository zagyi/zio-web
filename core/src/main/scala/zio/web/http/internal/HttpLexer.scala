package zio.web.http.internal

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
