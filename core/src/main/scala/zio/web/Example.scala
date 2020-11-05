package zio.web

import zio.web.codec._

trait Example extends http.HttpProtocolModule {
  import http.HttpMiddleware

  sealed case class UserId(id: String)
  sealed case class UserProfile(age: Int, fullName: String, address: String)

  val userJoe: UserId = UserId("123123")

  val userIdCodec: Codec[UserId] = Codec.caseClassN("id" -> Codec[String])(UserId(_), UserId.unapply(_))

  val userProfileCodec: Codec[UserProfile] = Codec.caseClassN(
    "age"      -> Codec[Int],
    "fullName" -> Codec[String],
    "address"  -> Codec[String]
  )(UserProfile(_, _, _), UserProfile.unapply(_))

  lazy val getUserProfile: Endpoint2[Any, UserId, UserProfile] =
    endpoint("getUserProfile").withRequest(userIdCodec).withResponse(userProfileCodec).handler(_ => ???) @@ Route(
      "/users/"
    )

  lazy val setUserProfile =
    endpoint("setUserProfile")
      .withRequest(Codec.zipN(userIdCodec, userProfileCodec))
      .withResponse(Codec[Unit])
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
