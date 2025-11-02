# client-workers-quarkus

Quarkus-based example worker module demonstrating a client worker application.

This module shows how to configure a Quarkus application that uses the `taktx-client-quarkus` client and deploys
BPMN resources from `src/main/resources/bpmn/`.

## Prerequisites
- JDK 21+
- Gradle 8.14+ (or use the included Gradle Wrapper in the root of this repository)
- A running TaktX engine instance (locally or remotely). You can use the [
- TaktX Docker Compose setup](../docker/docker-compose-full/README.md) to quickly spin up a local TaktX environment.
- (Optional) An IDE with Quarkus support for easier development (e.g., IntelliJ IDEA, VS Code).
- (Recommended) Familiarity with Quarkus framework and TaktX concepts (BPMN, service tasks, workers).

## Quick commands (zsh)

- Build this module from the root of this repository:

```bash
./gradlew :client-workers-quarkus:build
```

- Run Quarkus in dev mode (hot-reload):

```bash
./gradlew :client-workers-quarkus:quarkusDev
```

- Run the module's tests:

```bash
./gradlew :client-workers-quarkus:test
```

## What you'll find here

- `src/main/java/io/taktx/app/instanceupdates/EventObserver.java` - Observes the event stream. Each InstanceUpdateRecord is passed to this observer.
- `src/main/java/io/taktx/app/Main.java` — application entrypoint annotated with `@QuarkusMain` and `@Deployment` pointing towards the BPMN files location.
- `src/main/java/io/taktx/app/workers/ServiceTaskWorker.java` — example service task worker implementation using the TaktX Quarkus client.
- `src/main/resources/application.properties` — module config (e.g., `quarkus.http.port`, `taktx.engine.namespace`).
- `src/main/resources/bpmn/` — BPMN resources for the deployment annotation (if present).
