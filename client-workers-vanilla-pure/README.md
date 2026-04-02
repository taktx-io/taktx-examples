# client-workers-vanilla-pure

A plain Java (no framework) example worker application demonstrating how to implement TaktX external
task workers using the **raw `ExternalTaskTriggerConsumer` API** — no annotations, no DI framework.

This is the most explicit integration style. You implement `ExternalTaskTriggerConsumer` directly,
dispatch on `externalTaskId`, and call `taktXClient.respondToExternalTask(...)` yourself. It gives
you full control over every aspect of task handling.

## Prerequisites

- **JDK 21+**
- **Gradle 8.x** or use the included Gradle Wrapper in the root of this repository
- A running TaktX engine instance — use the [Docker Compose setup](../docker/docker-compose-full/README.md)
  to quickly spin up a local environment

## Quick commands

```bash
# Build (from the repository root)
./gradlew :client-workers-vanilla-pure:build

# Run
./gradlew :client-workers-vanilla-pure:run

# Run tests
./gradlew :client-workers-vanilla-pure:test
```

## What you'll find here

| File | Description |
|------|-------------|
| `src/main/java/io/taktx/app/Main.java` | Entry point — builds the client, calls `start()`, and blocks until shutdown |
| `src/main/java/io/taktx/app/TaktXClientProvider.java` | Builds and configures `TaktXClient`: requests external task topics, deploys the BPMN process definition, and registers the worker and instance update consumer |
| `src/main/java/io/taktx/app/MyServiceTaskWorker.java` | Implements `ExternalTaskTriggerConsumer`; declares handled task IDs and dispatches each batch to per-task handler methods |
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

### Implementing `ExternalTaskTriggerConsumer`

```java
public class MyServiceTaskWorker implements ExternalTaskTriggerConsumer {

    @Override
    public Set<String> getJobIds() {
        return Set.of("task1", "task2", "task3");
    }

    @Override
    public void acceptBatch(List<ExternalTaskTriggerDTO> triggers) {
        for (ExternalTaskTriggerDTO trigger : triggers) {
            switch (trigger.getExternalTaskId()) {
                case "task1" -> handleTask1(trigger);
                case "task2" -> handleTask2(trigger);
                case "task3" -> handleTask3(trigger);
            }
        }
    }
}
```

### Reading variables and responding

```java
private void handleTask1(ExternalTaskTriggerDTO trigger) {
    int intVar     = trigger.getVariables().get("intVar").asInt();
    String strVar  = trigger.getVariables().get("stringVar").asText();
    Map<String, String> headers = trigger.getHeaders();

    taktXClient.respondToExternalTask(trigger)
               .respondSuccess(new TestResultType(intVar, strVar));
}
```

### Topic registration

When task IDs are determined at runtime (e.g. via FEEL expressions in the BPMN), you must request
the external task topics explicitly before calling `start()`:

```java
taktXClient.requestExternalTaskTopic("task1", 3, CleanupPolicy.COMPACT, (short) 1);
taktXClient.requestExternalTaskTopic("task2", 3, CleanupPolicy.COMPACT, (short) 1);
taktXClient.requestExternalTaskTopic("task3", 3, CleanupPolicy.COMPACT, (short) 1);
```

## Security & Signing

Signing is **disabled by default** and all example workers operate normally without signing configuration.

To enable Ed25519 signing on this worker, set the signing environment variables before running:

```bash
export TAKTX_SIGNING_IDENTITY_SOURCE=env
export TAKTX_SIGNING_KEY_ID=vanilla-pure-key-1
export TAKTX_SIGNING_PRIVATE_KEY=$(cat private-key.b64)
export TAKTX_SIGNING_PUBLIC_KEY=$(cat public-key.b64)
```

See [`docs/security.md`](../docs/security.md) for key generation steps and how to enable signing
on the engine.

## License

This module is licensed under the [Apache License 2.0](LICENSE).
