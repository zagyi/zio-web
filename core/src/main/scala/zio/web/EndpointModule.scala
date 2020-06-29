package zio.web

import zio._
import zio.web.docs._

/*

In gRPC, services are described by 1 or more "methods" with a name, e.g.:

  rpc HelloWorld(HelloRequest) returns (HelloResponse);

  rpc GetUserProfile(UserIdRequest) returns (UserProfileResponse)

In HTTP, services are described by 1 or more routes, consisting of HTTP Method, URL pattern, header pattern, e.g.:

  GET /users/{id}
  Content-type: application/json

  POST /users-service/get-user-profile?userId=123

In Thrift, services are described by 1 or more "methods" with a name, e.g.:

  string helloWorld(string input)

 */
trait EndpointModule extends codec.CodecModule {

  trait ClientService[A] {

    def lookup[Request, Response](endpoint: Endpoint[Request, Response])(
      implicit ev: A <:< Endpoint[Request, Response]
    ): Request => Task[Response]
  }

  sealed case class Endpoint[Request, Response](
    endpointName: String,
    doc: Doc,
    request: Codec[Request],
    response: Codec[Response]
  ) { self =>

    /**
     * Returns a new endpoint that attaches additional documentation to this
     * endpoint.
     */
    def ??(details: Doc): Endpoint[Request, Response] = copy(doc = doc <> details)

    /**
     * Returns a new endpoint that attaches additional (string) documentation
     * to this endpoint.
     */
    def ??(details: String): Endpoint[Request, Response] = copy(doc = doc <> Doc(details))

    def withRequest[Request2](r: Codec[Request2]): Endpoint[Request2, Response] =
      mapRequest(_ => r)

    def withResponse[Response2](r: Codec[Response2]): Endpoint[Request, Response2] =
      mapResponse(_ => r)

    def mapRequest[Request2](f: Codec[Request] => Codec[Request2]): Endpoint[Request2, Response] =
      copy(request = f(request))

    def mapResponse[Response2](f: Codec[Response] => Codec[Response2]): Endpoint[Request, Response2] =
      copy(response = f(response))

    /**
     * Returns a new endpoint that adds the specified request information
     * into the request required by this endpoint.
     */
    def request[Request2](request2: Codec[Request2]): Endpoint[(Request, Request2), Response] =
      copy(request = zipCodec(request, request2))

    /**
     * Returns a new endpoint that adds the specified response information
     * into the response produced by this endpoint.
     */
    def response[Response2](response2: Codec[Response2]): Endpoint[Request, (Response, Response2)] =
      copy(response = zipCodec(response, response2))
  }

  /**
   * Constructs a new endpoint with the specified name.
   */
  final def endpoint(name: String): Endpoint[Unit, Unit] =
    Endpoint(name, Doc.Empty, unitCodec, unitCodec)

  /**
   * Constructs a new endpoint with the specified name and text documentation.
   */
  final def endpoint(name: String, text: String): Endpoint[Unit, Unit] =
    endpoint(name) ?? text

  trait Endpoints[A] { self =>
    def ::[I, O](that: Endpoint[I, O]): Endpoints[Endpoint[I, O] with A] = Endpoints.Cons[I, O, A](that, self)
  }

  object Endpoints {
    private[web] case object Empty extends Endpoints[Any]
    sealed private[web] case class Cons[I, O, X](head: Endpoint[I, O], tail: Endpoints[X])
        extends Endpoints[Endpoint[I, O] with X]

    val empty: Endpoints[Any] = Empty
  }

  /**
   * A model of a service, which has a name, documentation, and a collection
   * of endpoints.
   */
  sealed case class Service[A](serviceName: String, doc: Doc, endpoints: Endpoints[A]) { self =>

    /**
     * Returns a new service that attaches additional documentation to this
     * service.
     */
    def ??(details: Doc): Service[A] = copy(doc = doc <> details)

    /**
     * Returns a new service that attaches additional (string) documentation
     * to this service.
     */
    def ??(details: String): Service[A] = copy(doc = doc <> Doc(details))

    /**
     * Returns a new service that attaches additional endpoints to this
     * service.
     */
    def endpoint[I, O](e: Endpoint[I, O]): Service[Endpoint[I, O] with A] =
      copy(endpoints = e :: endpoints)

    def invoke[I, O](endpoint: Endpoint[I, O]): InvokeApply[A, I, O] = new InvokeApply[A, I, O](endpoint)
  }

  class InvokeApply[A, I, O](endpoint: Endpoint[I, O]) {

    def apply(i: I)(implicit ev: A <:< Endpoint[I, O], t: zio.Tagged[A]): RIO[Has[ClientService[A]], O] = {
      val _ = t
      ZIO.accessM[Has[ClientService[A]]](_.get.lookup(endpoint)(ev)(i))
    }
  }

  /**
   * Builds a new service without any endpoints.
   */
  final def service(name: String): Service[Any] =
    Service(name, Doc.Empty, Endpoints.empty)

  /**
   * Builds a new service without any endpoints, but with the specified
   * text description.
   */
  final def service(name: String, text: String): Service[Any] =
    service(name) ?? text

  type Handler[-R, -A, +B] = A => zio.RIO[R, B]
  sealed trait Handlers[-R, A] { self =>

    def ::[R1 <: R, I, O](that: Handler[R1, I, O]): Handlers[R1, Endpoint[I, O] with A] =
      Handlers.Cons[R1, I, O, A](that, self)

    // TODO: Add :: for Handler2, Handler3, Handler4, etc., which auto-tuple
  }

  object Handlers {
    private[web] case object Empty extends Handlers[Any, Any]
    sealed private[web] case class Cons[R, I, O, X](head: Handler[R, I, O], tail: Handlers[R, X])
        extends Handlers[R, Endpoint[I, O] with X]

    val empty: Handlers[Any, Any] = Empty
  }
}
