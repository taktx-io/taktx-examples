# client-workers-quarkus

A Quarkus-based example worker application demonstrating how to implement TaktX external task
workers using the `taktx-client-quarkus` CDI auto-configuration module and `@JobWorker` annotations.

Quarkus handles the full lifecycle: the `TaktXClient` bean is produced from `application.properties`,
the client is started when the application starts up, and BPMN resources are deployed automatically
via the `@Deployment` annotation on `Main`.

## Prerequisites

- **JDK 21+**
- **Gradle 8.x** or use the included Gradle Wrapper in the root of this repository
- A running TaktX engine instance — use the [Docker Compose setup](../docker/docker-compose-full/README.md)
  to quickly spin up a local environment

## Quick commands

```bash
# Build (from the repository root)
./gradlew :client-workers-quarkus:build

# Run in dev mode (hot-reload)
./gradlew :client-workers-quarkus:quarkusDev

# Run tests
./gradlew :client-workers-quarkus:test
```

## What you'll find here

| File | Description |
|------|-------------|
| `src/main/java/io/taktx/app/Main.java` | Application entry point; `@QuarkusMain` + `@Deployment(resources = "classpath:bpmn/*.bpmn")` — deploys all BPMN files on startup |
| `src/main/java/io/taktx/app/workers/ServiceTaskWorker.java` | `@ApplicationScoped` bean with three `@JobWorker` methods (`task1`, `task2`, `task3`) demonstrating variable binding, custom headers, auto-complete, and manual completion via `ExternalTaskInstanceResponder` |
| `src/main/java/io/taktx/app/instanceupdates/EventObserver.java` | `InstanceUpdateConsumer` that receives every process state change event |
| `src/main/resources/application.properties` | Quarkus / TaktX configuration (port, tenant-id, namespace, Kafka bootstrap) |
| `src/main/resources/bpmn/servicetasks.bpmn` | Main example BPMN process with service tasks |
| `src/main/resources/bpmn/subprocess.bpmn` | BPMN process with a sub-process |
| `src/main/resources/bpmn/parallel-multi.bpmn` | BPMN process with a parallel multi-instance pattern |

## Configuration

```properties
# src/main/resources/application.properties
quarkus.http.port=8081

# Must match the engine's tenant-id and namespace
taktx.engine.tenant-id=acme
taktx.engine.namespace=default

taktx.client.groupId.instanceupdate=taktx-client
bootstrap.servers=localhost:9092
```

> Make sure `taktx.engine.tenant-id` and `taktx.engine.namespace` match the values configured
> on the engine (see [Docker Compose README](../docker/docker-compose-full/README.md)).

## Worker implementation highlights

### `@JobWorker` annotations

Worker methods are discovered automatically by the Quarkus CDI integration. Parameters are
resolved by type and by `@Variable` / `@CustomHeaders` annotations:

```java
@JobWorker(type = "task1", autoComplete = true,
           threadingStrategy = ThreadingStrategy.VIRTUAL_THREAD_FIRE_AND_FORGET,
           ackStrategy = AckStrategy.IMPLICIT)
public TestResultType task1(
    int intVar,
    String stringVar,
    Map<String, Object> allVariablesMap,
    @Variable("mapVar") Map<String, Object> singleVarMap,
    @CustomHeaders Map<String, String> headers,
    List<String> listVar) { ... }
```

### Manual task completion

Set `autoComplete = false` and accept an `ExternalTaskInstanceResponder` parameter to control
when and how the task is completed (success, BPMN error, escalation, or promise):

```java
@JobWorker(type = "task3", autoComplete = false, ...)
public void task3(Integer result3, String result4,
                  ExternalTaskInstanceResponder responder) {
    responder.respondSuccess(new TestResultType(result3, result4));
}
```

### BPMN error handling

Throw a `TaktXBpmnError` to trigger a BPMN error boundary event on the process:

```java
throw new TaktXBpmnError(false, "errorCode", "message", VariablesDTO.empty());
```

## Security & Signing

Signing is **disabled by default**. The worker connects and processes tasks without any signing
configuration.

To enable Ed25519 signing, set the following environment variables before starting the worker:

```bash
# Use a generated key for local dev (default — nothing to configure)

# Use a stable key from environment variables (recommended for non-dev)
export TAKTX_SIGNING_IDENTITY_SOURCE=env
export TAKTX_SIGNING_KEY_ID=my-quarkus-worker-key-1
export TAKTX_SIGNING_PRIVATE_KEY=$(cat private-key.b64)
export TAKTX_SIGNING_PUBLIC_KEY=$(cat public-key.b64)
```

Then enable signing on the running engine:

```java
TaktXClient.publishGlobalConfig(props,
    GlobalConfigurationDTO.builder().signingEnabled(true).build());
```

See [`docs/security.md`](../docs/security.md) for key generation steps and production configuration.

## License

This module is licensed under the [Apache License 2.0](LICENSE).
