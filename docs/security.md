# Security & Signing — Getting Started

**Applies to:** TaktX engine `0.4.0-beta-1` · TaktX client `0.4.0-beta-1`

TaktX 0.4.0-beta-1 ships two opt-in security mechanisms:

| Mechanism | What it protects | Enabled by |
|-----------|-----------------|------------|
| **Ed25519 message signing** | Authenticity of engine outbound records and worker responses | `signingEnabled=true` in the `taktx-configuration` topic |
| **RS256 JWT command authorization** | Entry commands (`StartCommandDTO`, `AbortTriggerDTO`, `SetVariableTriggerDTO`) | `engineRequiresAuthorization=true` in the `taktx-configuration` topic |

Both are **off by default**. A fresh deployment processes BPMN normally with no signing configuration needed.

---

## Contents

1. [Enable signing at runtime](#1-enable-signing-at-runtime)
2. [Worker signing identity — quick start](#2-worker-signing-identity--quick-start)
3. [Worker signing identity — production](#3-worker-signing-identity--production)
4. [Engine signing identity](#4-engine-signing-identity)
5. [RS256 JWT authorization](#5-rs256-jwt-authorization)
6. [Anchored mode (chain-of-trust)](#6-anchored-mode-chain-of-trust)
7. [Trust metadata on instance updates](#7-trust-metadata-on-instance-updates)
8. [Environment variable reference](#8-environment-variable-reference)

---

## 1. Enable signing at runtime

Signing is toggled via a `GlobalConfigurationDTO` record on the `taktx-configuration` Kafka topic.
No engine or worker restart is required — the change takes effect within one Kafka poll cycle.

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("taktx.engine.tenant-id", "acme");
props.put("taktx.engine.namespace", "default");

// Enable signing + authorization
TaktXClient.publishGlobalConfig(props,
    GlobalConfigurationDTO.builder()
        .signingEnabled(true)
        .engineRequiresAuthorization(true)
        .build());

// Disable signing (e.g. during rollout)
TaktXClient.publishGlobalConfig(props,
    GlobalConfigurationDTO.builder()
        .signingEnabled(false)
        .engineRequiresAuthorization(false)
        .build());
```

> **Note:** `exp` (JWT expiry) rejection and `auditId` replay protection are **always enforced**,
> regardless of the `engineRequiresAuthorization` flag.

---

## 2. Worker signing identity — quick start

By default the TaktX client generates a fresh Ed25519 key pair at startup (`generated` source).
This is suitable for local development and the included example modules — no configuration needed.

```properties
# application.properties — no signing config needed for local dev
bootstrap.servers=localhost:9092
taktx.engine.tenant-id=acme
taktx.engine.namespace=default
```

The worker automatically publishes its generated public key to the `taktx-signing-keys` topic
when `TaktXClient.start()` is called.

---

## 3. Worker signing identity — production

For production, use `env` or `file` source so the key identity is stable across restarts.

### Option A — Environment variables (`env` source)

Generate a key pair:

```bash
# Generate via temporary PEM — works on macOS and Linux
openssl genpkey -algorithm Ed25519 -out /tmp/taktx-key.pem

# Export private key (PKCS#8 DER, base64)
openssl pkey -in /tmp/taktx-key.pem -outform DER | base64 | tr -d '\n' > private-key.b64

# Export public key (X.509 DER, base64)
openssl pkey -in /tmp/taktx-key.pem -pubout -outform DER | base64 | tr -d '\n' > public-key.b64

echo "my-worker-key-1" > key-id

rm /tmp/taktx-key.pem
```

Set environment variables (or Kubernetes Secret):

```bash
export TAKTX_SIGNING_IDENTITY_SOURCE=env
export TAKTX_SIGNING_KEY_ID=my-worker-key-1
export TAKTX_SIGNING_PRIVATE_KEY=$(cat private-key.b64)
export TAKTX_SIGNING_PUBLIC_KEY=$(cat public-key.b64)
export TAKTX_SIGNING_OWNER=my-worker   # optional, human-readable label
```

### Option B — File source (supports live key rotation)

```bash
export TAKTX_SIGNING_IDENTITY_SOURCE=file
export TAKTX_SIGNING_FILE_KEY_ID_PATH=/opt/taktx/signing/key-id
export TAKTX_SIGNING_FILE_PRIVATE_KEY_PATH=/opt/taktx/signing/private-key.b64
export TAKTX_SIGNING_FILE_PUBLIC_KEY_PATH=/opt/taktx/signing/public-key.b64
# Optional: how often to poll for key file changes (default 1000 ms)
# export TAKTX_SIGNING_FILE_REFRESH_INTERVAL_MS=1000
```

Write key file updates atomically to avoid partial reads:

```bash
cp new-private-key.b64 /opt/taktx/signing/private-key.b64.tmp
mv -f /opt/taktx/signing/private-key.b64.tmp /opt/taktx/signing/private-key.b64
# repeat for public-key.b64 and key-id
```

---

## 4. Engine signing identity

The engine also uses Ed25519 for signing its outbound records.
In the Docker Compose setup it defaults to `generated`.
Configure persistent signing by editing `docker/docker-compose-full/docker-compose.yaml`:

```yaml
environment:
  - TAKTX_SIGNING_IDENTITY_SOURCE=file
  - TAKTX_SIGNING_FILE_KEY_ID_PATH=/opt/taktx/signing/engine/key-id
  - TAKTX_SIGNING_FILE_PRIVATE_KEY_PATH=/opt/taktx/signing/engine/private-key.b64
  - TAKTX_SIGNING_FILE_PUBLIC_KEY_PATH=/opt/taktx/signing/engine/public-key.b64
volumes:
  - ./signing/engine:/opt/taktx/signing/engine:ro
```

---

## 5. RS256 JWT authorization

When `engineRequiresAuthorization=true`, entry commands must carry a valid RS256 JWT in the
`X-TaktX-Authorization` Kafka record header.

### Publishing a JWT verification key

```java
// Publish once; re-publish on rotation.
TaktXClient.publishSigningKey(
    props,
    "platform-key-2026-03",   // must match JWT kid exactly
    rsaPublicKeyBase64,        // X.509 DER, base64-encoded
    "platform",
    "RSA");
```

### JWT requirements

| Field | Requirement |
|-------|-------------|
| `kid` header | Must match a published RSA key in `taktx-signing-keys` |
| `exp` | Must be in the future — **always enforced** |
| `auditId` | Must be unique — **always enforced** (replay protection) |
| Algorithm | RS256 |
| Action claims | Must match the inbound command type |

---

## 6. Anchored mode (chain-of-trust)

In **community mode** (default), any non-revoked key is trusted — the security boundary is Kafka ACLs.

In **anchored mode**, every key in the `taktx-signing-keys` topic must carry an RSA/SHA-256
countersignature from a platform root private key. Anchored mode is activated on the engine by
setting `TAKTX_PLATFORM_PUBLIC_KEY`.

This is an advanced scenario intended for multi-tenant production deployments. The full setup
procedure is documented in the [TaktX Engine security reference](https://github.com/taktx-io/TaktX-engine/blob/main/docs/security.md).

### Quick summary

```bash
# Step 1 — Generate the platform root RSA key pair (once per deployment)
scripts/generate_trust_anchor.sh --init
# → docker/signing/platform-private.pem  (KEEP SECRET — never commit)
# → docker/signing/platform-public.b64   (→ TAKTX_PLATFORM_PUBLIC_KEY)

# Step 2 — Sign a worker key
scripts/generate_trust_anchor.sh --sign \
  --key-dir /path/to/worker-keys \
  --owner my-worker \
  --role CLIENT
# → copy TAKTX_SIGNING_REGISTRATION_SIGNATURE=... into worker environment
```

Set `TAKTX_SIGNING_REGISTRATION_SIGNATURE` on the worker alongside the usual `TAKTX_SIGNING_*` variables.

---

## 7. Trust metadata on instance updates

`InstanceUpdateDTO` exposes two provenance fields from 0.4.0-beta-1:

| Field | Meaning |
|-------|---------|
| `currentTrustMetadata` | Trust data for the command currently being processed |
| `originTrustMetadata` | Trust data for the original command that started the chain |

Use these in your `InstanceUpdateConsumer` to build audit trails:

```java
client.registerInstanceUpdateConsumer("my-consumer", record -> {
    var current = record.getInstanceUpdate().getCurrentTrustMetadata();
    var origin  = record.getInstanceUpdate().getOriginTrustMetadata();
    logger.info("Event from {} (origin: {})", current, origin);
});
```

> The legacy `commandTrustMetadata` accessor still works and resolves to `currentTrustMetadata`.
> New code should prefer the explicit `current`/`origin` fields.

---

## 8. Environment variable reference

### Worker / client

| Variable | Purpose | Default |
|----------|---------|---------|
| `TAKTX_SIGNING_IDENTITY_SOURCE` | `env`, `file`, or `generated` | auto-detected |
| `TAKTX_SIGNING_KEY_ID` | Key ID (env source) | — |
| `TAKTX_SIGNING_PRIVATE_KEY` | Base64 PKCS#8 DER Ed25519 private key (env source) | — |
| `TAKTX_SIGNING_PUBLIC_KEY` | Base64 X.509 DER Ed25519 public key (env source) | — |
| `TAKTX_SIGNING_OWNER` | Human-readable label for the published key | app name |
| `TAKTX_SIGNING_FILE_KEY_ID_PATH` | Path to `key-id` file (file source) | — |
| `TAKTX_SIGNING_FILE_PRIVATE_KEY_PATH` | Path to `private-key.b64` file (file source) | — |
| `TAKTX_SIGNING_FILE_PUBLIC_KEY_PATH` | Path to `public-key.b64` file (file source) | — |
| `TAKTX_SIGNING_FILE_REFRESH_INTERVAL_MS` | File refresh interval in ms | `1000` |
| `TAKTX_SIGNING_REGISTRATION_SIGNATURE` | Countersignature for anchored mode | — |

### Engine (additional)

| Variable | Purpose |
|----------|---------|
| `TAKTX_PLATFORM_PUBLIC_KEY` | Activates anchored mode; base64 DER RSA public key |
| `TAKTX_ENGINE_KEY_REGISTRATION_SIGNATURE` | Countersignature for the engine's own key (anchored mode) |

### Kafka auth / TLS (engine and workers)

| Variable | Purpose |
|----------|---------|
| `KAFKA_SECURITY_PROTOCOL` | e.g. `SASL_SSL`, `PLAINTEXT` |
| `KAFKA_SASL_MECHANISM` | e.g. `SCRAM-SHA-512` |
| `KAFKA_SASL_JAAS_CONFIG` | Full JAAS config string |
| `KAFKA_SSL_TRUSTSTORE_LOCATION` | Path to JKS/PKCS12 truststore |
| `KAFKA_SSL_TRUSTSTORE_PASSWORD` | Truststore password |
| `KAFKA_SSL_TRUSTSTORE_TYPE` | `JKS` or `PKCS12` |

