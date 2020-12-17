/*
 *
 *  Copyright 2017-2020 John A. De Goes and the ZIO Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package zio.web.http.model

import zio.{ Chunk, NonEmptyChunk }

/**
 * Conforms to https://www.w3.org/Addressing/URL/uri-spec.html
 *
 * See syntax tree:
 * https://en.wikipedia.org/wiki/Uniform_Resource_Identifier#/media/File:URI_syntax_diagram.svg
 */
// TODO: add tests for various cases
final case class Uri(
  scheme: Option[Scheme],
  authority: Authority,
  path: Path,
  query: Query,
  fragment: Option[Fragment]
) {
  override def toString: String =
    scheme.map(_.repr + "://").getOrElse("") +
      authority.toString +
      path.repr.mkString("") +
      query.repr.map("?" + _.mkString("&")).getOrElse("") +
      fragment.map("#" + _.repr).getOrElse("")
}

final case class Scheme(repr: String) extends AnyVal
final case class Authority(userInfo: Option[UserInfo], host: Option[Host], port: Option[Port]) {
  override def toString: String =
    userInfo.map(_.repr + "@").getOrElse("") +
      host.getOrElse("") +
      port.map(":" + _.repr.toString).getOrElse("")
}
final case class UserInfo(repr: String)                     extends AnyVal
final case class Host(repr: String)                         extends AnyVal
final case class Port(repr: Int)                            extends AnyVal
final case class Path(repr: Chunk[String])                  extends AnyVal
final case class Query(repr: Option[NonEmptyChunk[String]]) extends AnyVal
final case class Fragment(repr: String)                     extends AnyVal

object Uri {

  def fromString(uri: String): Uri = {
    val mediator = new java.net.URI(uri)
    val scheme = mediator.getScheme match {
      case null => Option.empty
      case s    => Option(Scheme(s))
    }
    val userInfo = mediator.getUserInfo match {
      case null => Option.empty
      case u    => Option(UserInfo(u))
    }
    val host = mediator.getHost match {
      case null => Option.empty
      case h    => Option(Host(h))
    }
    val port = mediator.getPort match {
      case -1 => Option.empty
      case p  => Option(Port(p))
    }
    val query = mediator.getQuery match {
      case null => Query(Option.empty)
      case q    => Query(Option(NonEmptyChunk[String](q)))
    }
    val fragment = mediator.getFragment match {
      case null => Option.empty
      case f    => Option(Fragment(f))
    }
    Uri(
      scheme,
      Authority(userInfo, host, port),
      Path(Chunk.fromArray(mediator.getPath.split(""))),
      query,
      fragment
    )
  }
}
