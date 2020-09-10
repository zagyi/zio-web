package zio.web

trait Example extends http.HttpProtocolModule {
  import http.HttpMiddleware

  sealed case class UserId(id: String)
  sealed case class UserProfile(age: Int, fullName: String, address: String)

  lazy val userJoe: UserId = ???

  lazy val userIdCodec: Codec[UserId]           = ???
  lazy val userProfileCodec: Codec[UserProfile] = ???

  lazy val getUserProfile: Endpoint2[Any, UserId, UserProfile] =
    endpoint("getUserProfile").withRequest(userIdCodec).withResponse(userProfileCodec).handler(_ => ???) @@ Route(
      "/users/"
    )

  lazy val setUserProfile =
    endpoint("setUserProfile")
      .withRequest(zipCodec(userIdCodec, userProfileCodec))
      .withResponse(unitCodec)
      .handler(_ => ???)

  lazy val userService =
    getUserProfile ::
      setUserProfile :: Endpoints.empty

  object client_example {
    lazy val userProfile = userService.invoke(getUserProfile)(userJoe).provideLayer(makeClient(userService))
  }

  object server_example {
    lazy val serverLayer = makeServer(HttpMiddleware.none, userService)
  }

  object docs_example {
    val docs = makeDocs(userService)
  }
}
