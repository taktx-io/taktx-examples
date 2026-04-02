# client-swagger-ui

A Quarkus REST application that exposes the TaktX client operations via **Swagger UI**.
Use it to interactively start process instances, inspect deployed BPMN definitions, and send signals
to running process instances — all through a browser, without writing any client code.

## Prerequisites

- **JDK 21+**
- **Gradle 8.x** or use the included Gradle Wrapper in the root of this repository
- A running TaktX engine instance — use the [Docker Compose setup](../docker/docker-compose-full/README.md)
  to quickly spin up a local environment

## Quick commands

```bash
# Build (from the repository root)
./gradlew :client-swagger-ui:build

# Run in dev mode (hot-reload)
./gradlew :client-swagger-ui:quarkusDev

# Run tests
./gradlew :client-swagger-ui:test
```

Then open **`http://localhost:8081/q/swagger-ui/`** to explore the API.

## What you'll find here

| File | Description |
|------|-------------|
| `src/main/java/io/taktx/app/DefinitionResource.java` | REST endpoints to list and fetch deployed BPMN process definitions |
| `src/main/java/io/taktx/app/ProcessResource.java` | REST endpoints to start new process instances and trigger repeated process jobs |
| `src/main/java/io/taktx/app/SignalResource.java` | REST endpoints to send signals to running process instances |
| `src/main/resources/application.properties` | Quarkus / TaktX configuration (port, tenant-id, namespace, Kafka bootstrap) |

## Configuration

```properties
# src/main/resources/application.properties
quarkus.http.port=8081

# Must match the engine's tenant-id and namespace
taktx.engine.tenant-id=acme
taktx.engine.namespace=default

bootstrap.servers=localhost:9092
```

> Make sure `taktx.engine.tenant-id` and `taktx.engine.namespace` match the values configured
> on the engine (see [Docker Compose README](../docker/docker-compose-full/README.md)).

## Security & Signing

When `engineRequiresAuthorization=true` is enabled on the engine, the `StartCommandDTO` sent
through `ProcessResource` must carry a valid RS256 JWT. Configure the `AuthorizationTokenProvider`
in the application to attach JWTs automatically, or pass the token in the Swagger UI request body.

When signing is enabled (`signingEnabled=true`), this application can also sign its commands by
providing signing identity via environment variables:

```bash
export TAKTX_SIGNING_IDENTITY_SOURCE=env
export TAKTX_SIGNING_KEY_ID=swagger-ui-key-1
export TAKTX_SIGNING_PRIVATE_KEY=$(cat private-key.b64)
export TAKTX_SIGNING_PUBLIC_KEY=$(cat public-key.b64)
```

See [`docs/security.md`](../docs/security.md) for details.

## License

This module is licensed under the [Apache License 2.0](LICENSE).
