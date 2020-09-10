package zio.web

trait Example extends http.HttpProtocolModule {
  import http.HttpMiddleware

  sealed case class UserId(id: String)
  sealed case class UserProfile(age: Int, fullName: String, address: String)

  lazy val userJoe: UserId = ???

  lazy val userIdCodec: Codec[UserId]           = ???
  lazy val userProfileCodec: Codec[UserProfile] = ???

  lazy val getUserProfile =
    endpoint("getUserProfile").withRequest(userIdCodec).withResponse(userProfileCodec) @@ Route("/users/")

  lazy val setUserProfile =
    endpoint("setUserProfile").withRequest(zipCodec(userIdCodec, userProfileCodec)).withResponse(unitCodec)

  lazy val userService =
    service("users", "The user service allows retrieving and updating user profiles")
      .endpoint(getUserProfile.handler(_ => ???))
      .endpoint(setUserProfile.handler(_ => ???))

  object client_example {
    lazy val userProfile = userService.invoke(getUserProfile)(userJoe)
  }

  object server_example {
    lazy val serverLayer = makeServer(HttpMiddleware.none, userService)
  }

  object docs_example {
    val docs = makeDocs(userService)
  }
}
