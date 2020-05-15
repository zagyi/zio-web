package zio.web

trait Example extends ProtocolModule {
  trait UserId
  trait UserProfile

  lazy val userJoe: UserId = ???

  lazy val userIdCodec: Codec[UserId]           = ???
  lazy val userProfileCodec: Codec[UserProfile] = ???

  lazy val getUserProfile =
    endpoint("getUserProfile").asRequest(userIdCodec).asResponse(userProfileCodec)

  lazy val setUserProfile =
    endpoint("setUserProfile").asRequest(zipCodec(userIdCodec, userProfileCodec)).asResponse(unitCodec)

  lazy val userService =
    service("users", "The user service allows retrieving and updating user profiles")
      .endpoints(
        getUserProfile,
        setUserProfile
      )

  val result = getUserProfile(userJoe)
}
