---
id: index
title: "Index"
---

<!--
NOTE: These are ordered so that those with no dependencies are at the top and the ones with the most dependencies are at
the bottom.
-->

## Core Types

 - **[StandardType](standardtype.md)** — TODO.
 - **[Schema](schema.md)** — TODO.
 - **[Codec](codec.md)** — TODO.
 - **[Doc](doc.md)** — TODO.

## HTTP Types

 - **[Method](http/method.md)** — TODO.
 - **[Route](http/route.md)** — TODO.
 - **[Uri](http/uri.md)** — TODO.
 - **[HttpStatus](http/httpstatus.md)** — TODO.
 - **[HttpClientConfig](http/httpclientconfig.md)** — TODO.
 - **[HttpServerConfig](http/httpserverconfig.md)** — TODO.
 - **[HttpHeaders](http/httpheaders.md)** — TODO.
 - **[HttpRequest](http/httprequest.md)** — TODO.
   - **[Scheme](http/scheme.md)** — TODO.
 - **[HttpResponse](http/httpresponse.md)** — TODO.
 - **[HttpMiddleware](http/httpmiddleware.md)** — TODO.
   - **[Request](http/request.md)** — TODO.
   - **[RequestBuilder](http/requestbuilder.md)** — TODO.
   - **[Response](http/response.md)** — TODO.
   - **[Middleware](http/middleware.md)** — TODO.

## Protocol Modules

 - **[EndpointModule](modules/endpointmodule.md)** — TODO.
   - **[Annotations](modules/annotations.md)** — TODO.
   - **[Endpoint](modules/endpoint.md)** — TODO.
   - **[Endpoints](modules/endpoints.md)** — TODO.
   - **[ClientService](modules/clientservice.md)** — TODO.
 - **[ProtocolModule](modules/protocolmodule.md)** — TODO.
 - **[HttpProtocolModule](modules/httpprotocolmodule.md)** — TODO.

---

Here are some of the ZIO data type that you may come across:

 - **[Chunk]** — ZIO `Chunk`: Fast, Pure Alternative to Arrays.
 - **[Has]** - A `Has` is used to express an effect's dependency on a
 service of type `A`.
 - **[Ref]** — `Ref[A]` models a mutable reference to a value of type `A`.
 The two basic operations are `set`, which fills the `Ref` with a new value, and `get`, which retrieves its current
 content. All operations on a `Ref` are atomic and thread-safe, providing a reliable foundation for synchronizing
 concurrent programs.
 - **[ZIO]** — A `ZIO` is a value that models an effectful program, which
 might fail or succeed.
 - **[ZLayer]** - A `ZLayer` describes a layer of an application.

[chunk]: https://zio.dev/docs/datatypes/datatypes_chunk
[has]: https://zio.dev/docs/datatypes/datatypes_has
[ref]: https://zio.dev/docs/datatypes/datatypes_ref
[zio]: https://zio.dev/docs/datatypes/datatypes_io
[zlayer]: https://zio.dev/docs/datatypes/datatypes_zlayer
