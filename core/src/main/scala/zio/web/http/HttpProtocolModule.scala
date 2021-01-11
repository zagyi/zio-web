package zio.web.http

import zio._
import zio.web.http.model._
import zio.web._
import java.io.{ IOException, InputStream }
import java.net.{ HttpURLConnection, URL }

trait HttpProtocolModule extends ProtocolModule {
  type ServerConfig       = HttpServerConfig
  type ClientConfig       = HttpClientConfig
  type ServerService      = Any
  type Middleware[-R, +E] = HttpMiddleware[R, E]
  type MinMetadata        = Any
  type MaxMetadata        = Route with Method

  val defaultProtocol: codec.Codec

  val allProtocols: Map[String, codec.Codec]

  override def makeServer[M >: MaxMetadata <: MinMetadata, R <: Has[ServerConfig], E, A](
    middleware: Middleware[R, E],
    endpoints: Endpoints[M, A]
  ): ZLayer[R, IOException, Has[ServerService]] = ???

  override def makeDocs[M >: MaxMetadata <: MinMetadata](endpoints: Endpoints[M, _]): ProtocolDocs =
    ???

  override def makeClient[M >: MaxMetadata <: MinMetadata, A](
    endpoints: Endpoints[M, A]
  ): ZLayer[Has[ClientConfig], IOException, Has[ClientService[A]]] =
    ZLayer.service[ClientConfig].map { hasClientConfig =>
      val clientService: ClientService[A] = new ClientService[A] {
        override def invoke[M, Request, Response, H](endpoint: Endpoint[M, Request, Response, H], request: Request)(
          implicit ev: A <:< Endpoint[M, Request, Response, H]
        ): Task[Response] = {

          val requestToJson: Request => Option[String] = ???

          val responseFromJson: InputStream => Task[Response] = ???

          val endpointUrlPart = {
            def inAnnotation(annotations: Annotations[_]): Option[String] =
              annotations match {
                case Annotations.None              => None
                case Annotations.Cons(Route(r), _) => Some(r)
                case Annotations.Cons(_, tail)     => inAnnotation(tail)
              }

            inAnnotation(endpoint.annotations).getOrElse("")
          }

          val con = new URL(
            s"${hasClientConfig.get.host}:${hasClientConfig.get.host}/${endpointUrlPart.stripPrefix("/")}"
          ).openConnection
            .asInstanceOf[HttpURLConnection]

          requestToJson(request).foreach { strEncodedPayload =>
            con.setDoOutput(true);
            val os = con.getOutputStream();
            os.write(strEncodedPayload.getBytes());
            os.flush();
            os.close();
          }

          val requestMethodStr = {
            def inAnnotation(annotations: Annotations[_]): Option[Method] =
              annotations match {
                case Annotations.None                    => None
                case Annotations.Cons(method: Method, _) => Some(method)
                case Annotations.Cons(_, tail)           => inAnnotation(tail)
              }

            inAnnotation(endpoint.annotations).getOrElse(Method.GET).toString
          }

          con.setRequestMethod(requestMethodStr)
          if (con.getResponseCode == HttpURLConnection.HTTP_OK) {
            ZManaged.fromAutoCloseable(ZIO(con.getInputStream)).use { inputStream =>
              responseFromJson(inputStream)
            }
          } else {
            ZIO.fail(new Exception(s"Server responded with ${con.getResponseCode}"))
          }
        }
      }

      Has(clientService)
    }
}
