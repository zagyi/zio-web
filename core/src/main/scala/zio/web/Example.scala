package zio.web

trait Example extends ProtocolModule {
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
    val serverLayer = makeServer(userService)
  }

  object docs_example {
    val docs = makeDocs(userService)
  }
}
