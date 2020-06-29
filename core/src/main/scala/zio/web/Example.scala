package zio.web

trait Example extends http.HttpProtocolModule {
  import http.HttpMiddleware

  sealed case class UserId(id: String)
  sealed case class UserProfile(age: Int, fullName: String, address: String)

  lazy val userJoe: UserId = ???

  lazy val userIdCodec: Codec[UserId]           = ???
  lazy val userProfileCodec: Codec[UserProfile] = ???

  lazy val getUserProfile =
    endpoint("getUserProfile").withRequest(userIdCodec).withResponse(userProfileCodec)

  lazy val setUserProfile =
    endpoint("setUserProfile").withRequest(zipCodec(userIdCodec, userProfileCodec)).withResponse(unitCodec)

  lazy val userService =
    service("users", "The user service allows retrieving and updating user profiles")
      .endpoints(
        getUserProfile,
        setUserProfile
      )

  object client_example {
    val userProfile = getUserProfile(userJoe)
  }

  object server_example {
    val serverLayer = makeServer(HttpMiddleware.none, userService)
  }

  object docs_example {
    val docs = makeDocs(userService)
  }
}

/*

 Middleware-friendly
 * **Metrics/Monitoring**. Built-in integration with ZIO ZMX.
 * **Rate-limiting**. Customizable rate-limiting with DDOS protection.
 * Via third-party libraries, pluggable authentication, authorization, persistence, caching, session management

 */

trait Middleware {

  object http {
    // Non-invasive
    //
    //   - Extract out and do something with information from the protocol-level request
    //   - Extract out and do something with information from the protocol-level response
    //
    // Invasive
    //
    //    - Redirection / rewriting
    //    - Add new "endpoints"

    trait HttpRequest
    trait HttpResponse

    type ExecutableEndpoint = HttpRequest => zio.Task[HttpResponse]

    type Middleware = ExecutableEndpoint => ExecutableEndpoint

    val loggingMiddleware: Middleware =
      (ee: ExecutableEndpoint) =>
        (req: HttpRequest) => {
          ee(req)
        }
  }
}
