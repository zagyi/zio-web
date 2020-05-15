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
trait EndpointModule extends CodecModule {

  trait ClientService {
    def lookup[Request, Response](endpoint: Endpoint[Request, Response]): Request => Task[Response]
  }

  sealed case class Endpoint[Request, Response](
    endpointName: String,
    doc: Doc,
    request: Codec[Request],
    response: Codec[Response]
  ) extends (Request => ZIO[Has[ClientService], Throwable, Response]) { self =>
    final def apply(request: Request): ZIO[Has[ClientService], Throwable, Response] =
      ZIO.accessM[Has[ClientService]](_.get.lookup(self)(request))

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

    def asRequest[Request2](r: Codec[Request2]): Endpoint[Request2, Response] =
      mapRequest(_ => r)

    def asResponse[Response2](r: Codec[Response2]): Endpoint[Request, Response2] =
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

  /**
   * A model of a service, which has a name, documentation, and a collection
   * of endpoints.
   */
  sealed case class Service(serviceName: String, doc: Doc, endpoints: Chunk[Endpoint[_, _]]) {

    /**
     * Returns a new service that attaches additional documentation to this
     * service.
     */
    def ??(details: Doc): Service = copy(doc = doc <> details)

    /**
     * Returns a new service that attaches additional (string) documentation
     * to this service.
     */
    def ??(details: String): Service = copy(doc = doc <> Doc(details))

    /**
     * Returns a new service that attaches additional endpoints to this
     * service.
     */
    def endpoints(es: Endpoint[_, _]*): Service =
      copy(endpoints = endpoints ++ Chunk.fromIterable(es))
  }

  /**
   * Builds a new service without any endpoints.
   */
  final def service(name: String): Service =
    Service(name, Doc.Empty, Chunk.empty)

  /**
   * Builds a new service without any endpoints, but with the specified
   * text description.
   */
  final def service(name: String, text: String): Service =
    Service(name, Doc.Empty, Chunk.empty) ?? text
}
