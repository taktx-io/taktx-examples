# taktx-worker-examples

[![TaktX engine](https://img.shields.io/badge/TaktX%20engine-0.2.0--beta--1-blue)](https://github.com/taktx-io/TaktX-engine)
[![TaktX client](https://img.shields.io/badge/TaktX%20client-0.2.0--beta--1-blue)](https://github.com/taktx-io/TaktX-engine)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

A collection of example modules that demonstrate different ways to build and integrate worker
applications with [TaktX](https://taktx.io) — a high-performance, Kafka-backed BPMN 2.0 process
automation engine.

Each module is independently buildable and focuses on a specific integration style or framework.
The root project centralises common Gradle configuration and helper tasks so individual modules
stay lightweight.

## Current modules

| Module | Description |
|--------|-------------|
| [`client-workers-quarkus`](client-workers-quarkus/README.md) | Quarkus CDI worker using `taktx-client-quarkus` auto-configuration and `@JobWorker` annotations |
| [`client-workers-vanilla-annotations`](client-workers-vanilla-annotations/README.md) | Plain Java worker using `@JobWorker` annotations without a DI framework |
| [`client-workers-vanilla-pure`](client-workers-vanilla-pure/README.md) | Plain Java worker using the raw `ExternalTaskTriggerConsumer` API — no annotations |
| [`client-swagger-ui`](client-swagger-ui/README.md) | Quarkus REST application exposing a Swagger UI to interact with TaktX via the client |
| [`docker/docker-compose-full`](docker/docker-compose-full/README.md) | Docker Compose setup for a full local TaktX environment (engine, Kafka, Kafka UI, Grafana, Prometheus, console) |

## Prerequisites

- **JDK 21+**
- **Gradle 8.x** or use the included Gradle Wrapper (`./gradlew`)
- A running TaktX engine instance — use the [Docker Compose setup](docker/docker-compose-full/README.md) for a quick local environment

## Quick Start

### 1. Start a local TaktX environment

```bash
cd docker/docker-compose-full
docker compose up -d
```

This starts all services (engine + Kafka + console + observability) on the first run.
The TaktX engine is available at `http://localhost:8079`.

### 2. Run an example worker

```bash
# Quarkus worker (hot-reload, recommended for development)
./gradlew :client-workers-quarkus:quarkusDev

# Plain Java annotations worker
./gradlew :client-workers-vanilla-annotations:run

# Plain Java pure API worker
./gradlew :client-workers-vanilla-pure:run
```

### 3. Explore via Swagger UI

```bash
./gradlew :client-swagger-ui:quarkusDev
```

Then open `http://localhost:8081/q/swagger-ui/` to start process instances, inspect definitions,
and send signals interactively.

## Dependency versions

Managed centrally in [`gradle/libs.versions.toml`](gradle/libs.versions.toml):

 Library  Version 
------------------
 TaktX client  `0.2.0-beta-1` 
 Quarkus  `3.28.4` 
 Java  `21` 

## Security & Signing (new in 0.2.0-beta-1)

TaktX 0.4.0-beta-1 introduces two opt-in security mechanisms that can be enabled at runtime
without restarting the engine or workers:

- **Ed25519 message signing** — the engine and workers can cryptographically sign their Kafka
  records. Controlled by `signingEnabled` in the `taktx-configuration` topic.
- **RS256 JWT command authorization** — entry commands (`StartCommandDTO`, etc.) can require a
  valid RS256 JWT. Controlled by `engineRequiresAuthorization` in the `taktx-configuration` topic.
- **Trust metadata** — `InstanceUpdateDTO` now carries `currentTrustMetadata` and
  `originTrustMetadata` so you can trace who originally triggered a process chain.

Both mechanisms are **disabled by default**. All example modules work out of the box without
any signing configuration. See [`docs/security.md`](docs/security.md) for a step-by-step guide
to enabling signing in your worker applications.

## Repository structure

```
taktx-examples3/
├── build.gradle                    # Root Gradle configuration
├── settings.gradle                 # Module includes
├── gradle/
│   └── libs.versions.toml          # Centralised dependency versions
├── client-workers-quarkus/         # Quarkus CDI worker example
├── client-workers-vanilla-annotations/ # Plain Java @JobWorker example
├── client-workers-vanilla-pure/    # Plain Java raw-API example
├── client-swagger-ui/              # Swagger UI REST module
├── docker/
│   └── docker-compose-full/        # Full local environment
└── docs/
    └── security.md                 # Signing & security guide
```

## Common Gradle commands

```bash
# List all modules
./gradlew projects

# Build everything (all modules)
./gradlew clean build

# Build a single module (faster)
./gradlew :client-workers-quarkus:build

# Check for dependency updates
./gradlew checkDependencyUpdates
```

## License

This repository is licensed under the [Apache License 2.0](LICENSE).
