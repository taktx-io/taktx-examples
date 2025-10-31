# Takt BPMN Swagger UI Client

This is a simple static web server serving the Swagger UI to interact with 
TaktX using the TaktXClient.

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
./gradlew :client-swagger-ui:build
```

- Run Quarkus in dev mode (hot-reload):

```bash
./gradlew :client-workers-quarkus:quarkusDev
```

- Run the module's tests:

```bash
./gradlew :client-swagger-ui:test
```

## What you'll find here
There are three REST endpoint resources in this module showcasing how to interact 
with the TaktX client:
- DefinitionResource - to fetch deployed BPMN process definitions
- InstanceResource - to start new process instances or repeated process instance jobs
- SignalResource - to send signals to running process instances

The Swagger UI is available at `http://localhost:8080/q/swagger-ui/index.html` when running the module.

## Configuration
- `src/main/resources/application.properties` — module config (e.g., `quarkus.http.port`, `taktx.engine.namespace`).

## License
This repository is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE)
file for details.


