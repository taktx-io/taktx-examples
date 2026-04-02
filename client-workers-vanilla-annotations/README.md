# client-workers-vanilla-annotations

A plain Java (no framework) example worker application demonstrating how to implement TaktX external
task workers using `@JobWorker` annotations **without** a dependency injection framework.

The `TaktXClient` is built manually in `TaktXClientProvider`. The `AnnotationScanningExternalTaskTriggerConsumer`
scans the classpath for `@JobWorker`-annotated methods and wires them up automatically — giving you
the declarative annotation style without requiring Quarkus or Spring.

## Prerequisites

- **JDK 21+**
- **Gradle 8.x** or use the included Gradle Wrapper in the root of this repository
- A running TaktX engine instance — use the [Docker Compose setup](../docker/docker-compose-full/README.md)
  to quickly spin up a local environment

## Quick commands

```bash
# Build (from the repository root)
./gradlew :client-workers-vanilla-annotations:build

# Run
./gradlew :client-workers-vanilla-annotations:run

# Run tests
./gradlew :client-workers-vanilla-annotations:test
```

## What you'll find here

| File | Description |
|------|-------------|
| `src/main/java/io/taktx/app/Main.java` | Entry point — builds the client, calls `start()`, and blocks until shutdown |
| `src/main/java/io/taktx/app/TaktXClientProvider.java` | Builds and configures `TaktXClient`: sets up `AnnotationScanningExternalTaskTriggerConsumer`, deploys BPMN via `deployTaktDeploymentAnnotatedClasses()`, and registers the instance update consumer |
| `src/main/java/io/taktx/app/ServiceTaskWorker.java` | Three `@JobWorker` methods (`task1`, `task2`, `task3`) demonstrating primitive binding, map variables, custom deserialization, `@Variable`, `@CustomHeaders`, auto-complete, and manual completion |
| `src/main/java/io/taktx/app/MyInstanceUpdateConsumer.java` | Consumes process state change events (`InstanceUpdateDTO`) |
| `src/main/resources/application.properties` | Connection properties (Kafka bootstrap, namespace) |
| `src/main/resources/bpmn/servicetasks.bpmn` | Example BPMN process deployed at startup |

## Configuration

Update `src/main/resources/application.properties` (or the hardcoded values in `TaktXClientProvider`)
to point at your engine:

```properties
bootstrap.servers=localhost:9092
taktx.engine.namespace=namespace
```

> **Note:** `taktx.engine.tenant-id` is read from the properties file when configured.
> Make sure the `namespace` matches the engine's `TAKTX_ENGINE_NAMESPACE` value.

## Worker implementation highlights

### `@JobWorker` — annotation-driven, no DI

```java
@JobWorker(type = "task1", autoComplete = true)
public TestResultType task1(
    int intVar,
    String stringVar,
    Map<String, Object> allVariablesMap,
    @Variable("mapVar") Map<String, Object> singleVarMap,
    @Variable("mapVar") TestType deserializedVar,
    @CustomHeaders Map<String, String> headers,
    List<String> listVar) { ... }
```

Parameters are resolved by the `ParameterResolverFactory` from the client — the same mechanism
used by the Quarkus and Spring integrations.

### Manual task completion

```java
@JobWorker(type = "task3", autoComplete = false)
public void task3(int result3, String result4,
                  ExternalTaskInstanceResponder responder) {
    responder.respondSuccess(new TestResultType(result3, result4));
}
```

## Security & Signing

Signing is **disabled by default** and all example workers operate normally without signing configuration.

To enable Ed25519 signing on this worker, set the signing environment variables before running:

```bash
export TAKTX_SIGNING_IDENTITY_SOURCE=env
export TAKTX_SIGNING_KEY_ID=vanilla-annotations-key-1
export TAKTX_SIGNING_PRIVATE_KEY=$(cat private-key.b64)
export TAKTX_SIGNING_PUBLIC_KEY=$(cat public-key.b64)
```

See [`docs/security.md`](../docs/security.md) for key generation steps and how to enable signing
on the engine.

## License

This module is licensed under the [Apache License 2.0](LICENSE).
