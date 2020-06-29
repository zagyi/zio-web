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
      .endpoint(getUserProfile)
      .endpoint(setUserProfile)

  object client_example {
    lazy val userProfile = userService.invoke(getUserProfile)(userJoe)
  }

  object server_example {
    import zio._
    import zio.clock._

    lazy val getUserProfileHandler: UserId => ZIO[Clock, Nothing, UserProfile] = ???
    lazy val setUserProfileHandler: (UserId, UserProfile) => Task[Unit]        = ???

    lazy val handlers = getUserProfileHandler :: setUserProfileHandler :: Handlers.empty

    lazy val serverLayer = makeServer(HttpMiddleware.none, userService, handlers)
  }

  object docs_example {
    val docs = makeDocs(userService)
  }
}
