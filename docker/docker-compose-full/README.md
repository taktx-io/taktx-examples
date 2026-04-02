# Docker Compose — Full TaktX Community Environment

This Docker Compose setup provides a full local TaktX environment for development and testing.
It is the easiest way to bring up a TaktX engine together with supporting infrastructure —
including the community console — in a single command.

**Engine version:** `0.2.0-beta-1`
**Console:** TaktX Community Edition (pre-built images from GitHub Container Registry)

## Services

| Service                              | Profile(s)              | Default port | Description                                          |
|--------------------------------------|-------------------------|--------------|------------------------------------------------------|
| `kafka`                              | *(always)*              | 9092         | Single-node KRaft Kafka broker                       |
| `taktx`                              | `engine`, `full`        | 8079         | TaktX BPMN engine                                    |
| `kafka-ui`                           | `observability`, `full` | 8085         | Kafka UI — topic inspection and management           |
| `prometheus`                         | `observability`, `full` | 9090         | Prometheus metrics scraper                           |
| `grafana`                            | `observability`, `full` | 3000         | Grafana dashboards (pre-configured for TaktX)        |
| `taktx-community-ingester-inmemory`  | `console`, `full`       | —            | Reads process events from Kafka; maintains in-memory state |
| `taktx-community-platform-service`   | `console`, `full`       | 8084         | Community console REST API                           |
| `taktx-community-console-frontend`   | `console`, `full`       | 3001         | Community console UI (served directly)               |
| `nginx`                              | `console`, `full`       | 3002         | Reverse proxy — console frontend + API, same origin  |

## Prerequisites

- Docker and Docker Compose installed.
- A TaktX license file. Obtain one via the [TaktX website](https://taktx.io/contact). By default the
  compose file expects the license at `~/.taktx/license.lic`.
- Sufficient system resources to run multiple containers (2 GB RAM minimum for engine + Kafka).

## Quick Start

### Start everything (default)

```bash
cd docker/docker-compose-full
docker compose up -d
```

The included `.env` file sets `COMPOSE_PROFILES=full`, so **all services start by default** —
Kafka, TaktX engine, Kafka UI, Grafana, Prometheus, and the full TaktX Community Console
(ingester, platform service, frontend, and nginx).

No additional flags needed. Once all containers are healthy, open the console at
**http://localhost:3002**.

### Selective startup — start only a subset

Override the active profiles on the command line. This takes precedence over the `.env` default:

| Command | What starts |
|---------|-------------|
| `docker compose --profile engine up -d` | Kafka + TaktX engine |
| `docker compose --profile observability up -d` | Kafka + Kafka UI + Grafana + Prometheus |
| `docker compose --profile console up -d` | Kafka + community console (ingester, platform service, frontend, nginx) |

> **Note:** `kafka` has no profile and always starts regardless of the active profiles.

### Check status

```bash
docker compose ps
```

### Stop and remove containers

```bash
docker compose down
```

## Service URLs

| Service                      | URL                                        |
|------------------------------|--------------------------------------------|
| TaktX Engine                 | http://localhost:8079                      |
| TaktX Engine Swagger         | http://localhost:8079/q/swagger-ui/        |
| TaktX Engine health          | http://localhost:8079/q/health/ready       |
| Kafka (client)               | localhost:9092                             |
| Kafka UI                     | http://localhost:8085                      |
| Grafana                      | http://localhost:3000                      |
| Prometheus                   | http://localhost:9090                      |
| Community Console (via nginx)| http://localhost:3002                      |
| Platform Service API (direct)| http://localhost:8084                      |
| Console Frontend (direct)    | http://localhost:3001                      |

> **Recommended entry point for the console:** use nginx at **http://localhost:3002**.
> This serves the frontend and API from the same origin, eliminating CORS complexity.

## Community Console Architecture

The TaktX Community Console consists of three cooperating services:

```
Browser → nginx (3002)
              ├── / → taktx-community-console-frontend (3001)
              └── /api, /ws, /q → taktx-community-platform-service (8084)
                                        ↑
                          taktx-community-ingester-inmemory
                                (reads from Kafka 9094)
```

| Service | Image | Role |
|---------|-------|------|
| `taktx-community-ingester-inmemory` | `ghcr.io/taktx-io/taktx-community-ingester-inmemory:latest` | Kafka consumer — reads process events and maintains in-memory state |
| `taktx-community-platform-service` | `ghcr.io/taktx-io/taktx-community-platform-service:latest` | REST API and WebSocket server for the UI |
| `taktx-community-console-frontend` | `ghcr.io/taktx-io/taktx-community-console-frontend:latest` | Browser UI |

All images are pre-built and pulled automatically from the GitHub Container Registry. No local build step is required.

## Engine Configuration

The engine is configured by environment variables in `docker-compose.yaml`.
The minimum required variables are:

| Variable                      | Description                                           | Default in this compose     |
|-------------------------------|-------------------------------------------------------|-----------------------------|
| `KAFKA_BOOTSTRAP_SERVERS`     | Kafka bootstrap address (internal network)            | `kafka:9094`                |
| `TAKTX_ENGINE_TENANT_ID`      | Tenant prefix for all engine topics                   | `acme`                      |
| `TAKTX_ENGINE_NAMESPACE`      | Namespace prefix for all engine topics                | `default`                   |
| `TAKTX_LICENSE_FILE_LOCATION` | Path to the license file inside the container         | `/opt/taktx/licence.lic`    |

> **Topic naming:** All engine Kafka topics are named `<tenantId>.<namespace>.<topic>`.
> Make sure your worker applications use the same `tenant-id` and `namespace` values.

## Security & Signing (new in 0.2.0-beta-1)

TaktX 0.2.0-beta-1 introduces two opt-in security mechanisms.
Both are **disabled by default** — the engine starts and processes BPMN normally without any signing configuration.

### Ed25519 message signing

Workers can sign their responses and the engine signs its outbound records (instance updates, task triggers).
Enable at runtime by publishing a `GlobalConfigurationDTO` to the `taktx-configuration` Kafka topic:

```java
TaktXClient.publishGlobalConfig(props,
    GlobalConfigurationDTO.builder()
        .signingEnabled(true)
        .build());
```

The engine uses a **generated** Ed25519 key pair by default (suitable for local dev). For persistent key
identity across restarts, use `file` or `env` source — see the commented variables in `docker-compose.yaml`
and the [signing guide](../../docs/security.md).

### RS256 JWT command authorization

Restrict `StartCommandDTO`, `AbortTriggerDTO`, and `SetVariableTriggerDTO` to callers holding a valid RS256 JWT:

```java
TaktXClient.publishGlobalConfig(props,
    GlobalConfigurationDTO.builder()
        .engineRequiresAuthorization(true)
        .build());
```

### Enabling signing on the engine container

Uncomment and fill in the signing-related environment variables in `docker-compose.yaml`:

```yaml
- TAKTX_SIGNING_IDENTITY_SOURCE=file
- TAKTX_SIGNING_FILE_KEY_ID_PATH=/opt/taktx/signing/engine/key-id
- TAKTX_SIGNING_FILE_PRIVATE_KEY_PATH=/opt/taktx/signing/engine/private-key.b64
- TAKTX_SIGNING_FILE_PUBLIC_KEY_PATH=/opt/taktx/signing/engine/public-key.b64
```

Also uncomment the signing volume mount and place your key files in `./signing/engine/`.
See [docs/security.md](../../docs/security.md) for key generation instructions.

## Customisation

Edit `docker-compose.yaml` to change port mappings, volume paths, or environment variables.
The `prometheus.yml` and `grafana/` directory can be modified to adjust metrics collection and dashboards.

> **Ops Console (premium):** A separate `docker-compose-ops.yaml` with the premium Ops Console
> will be provided in a future release. The community console in this file focuses on the open
> community feature set.

## Troubleshooting

View logs for a specific service:

```bash
docker compose logs -f taktx
docker compose logs -f taktx-community-ingester-inmemory
docker compose logs -f taktx-community-platform-service
docker compose logs -f taktx-community-console-frontend
docker compose logs -f kafka
```

If the engine fails to start, the most common causes are:
- **Missing license file** — ensure `~/.taktx/license.lic` exists (or update the volume path in `docker-compose.yaml`).
- **Kafka not ready** — the engine has a `depends_on` health check; wait for Kafka to become healthy before the engine starts.
- **Port conflict** — check that ports 8079, 9092, 8084, 8085, 3001, 3002, 9090 are not in use on your machine.

If the console is unreachable at http://localhost:3002:
- Check that all three console services are running: `docker compose ps`
- Check the ingester logs for Kafka connectivity issues: `docker compose logs taktx-community-ingester-inmemory`
- Check the platform service health: `curl http://localhost:8084/q/health/ready`

## Notes

This setup has no authentication configured for Kafka or the console. For production deployments,
add appropriate Kafka ACLs, TLS, and SASL configuration.

The TaktX Community Console images are community-edition builds. Premium features (advanced monitoring,
audit trails, multi-tenancy UI, etc.) are available in the Ops Console — watch for the `docker-compose-ops.yaml`
in a future release.

