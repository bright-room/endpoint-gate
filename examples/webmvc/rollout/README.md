# rollout

## Overview

This module demonstrates the gradual rollout feature of `endpoint-gate-spring-boot-starter`, covering configuration-based rollout and sticky rollout using a custom `EndpointGateContextResolver`.

## What This Example Demonstrates

- **Configuration-based rollout** -- Using `@EndpointGate("annotation-rollout")` on the controller and specifying the rollout percentage via `endpoint-gate.gates.<name>.rollout` in the configuration file (`AnnotationRolloutController`).
- **Configuration-based rollout** -- Specifying rollout percentage via `endpoint-gate.gates.<name>.rollout` in the configuration file (`ConfigRolloutController`).
- **Sticky rollout** -- Implementing a custom `EndpointGateContextResolver` that uses a user ID for deterministic rollout decisions (`StickyRolloutController`, `UserIdContextResolver`).
- **Rollout configuration** -- Rollout percentage is always configured via `endpoint-gate.gates.<name>.rollout` in YAML. The `@EndpointGate` annotation does not have a `rollout` attribute. The `RolloutPercentageProvider` value from the configuration is always used.

## How to Run

Start the application with the default profile (random, non-sticky rollout):

```bash
./gradlew :examples:webmvc:rollout:bootRun
```

To enable sticky rollout using user IDs:

```bash
./gradlew :examples:webmvc:rollout:bootRun --args='--spring.profiles.active=sticky'
```

> **Note:** The `EndpointGateContextResolver` is a single application-wide bean. Activating the `sticky` profile affects all rollout endpoints, not just the sticky-rollout endpoint.

## Endpoints

| Endpoint | Endpoint Gate | Rollout | Source | Expected Behavior |
|---|---|---|---|---|
| `GET /api/annotation-rollout` | `annotation-rollout` | 50% | Configuration | ~50% of requests succeed (random) |
| `GET /api/config-rollout` | `config-rollout` | 30% | Configuration | ~30% of requests succeed (random) |
| `GET /api/sticky-rollout` | `sticky-rollout` | 50% | Configuration | ~50% of users succeed (deterministic with `sticky` profile) |

With the `sticky` profile, pass a `userId` query parameter to get deterministic results:

```bash
curl http://localhost:8080/api/sticky-rollout?userId=user-123
```

The same `userId` will always produce the same result for the same gate.

## Configuration

### application.yml

```yaml
endpoint-gate:
  default-enabled: true
  gates:
    annotation-rollout:
      enabled: true
      rollout: 50
    config-rollout:
      enabled: true
      rollout: 30
    sticky-rollout:
      enabled: true
      rollout: 50
```

- `default-enabled: true` enables fail-open behavior, so gates not listed in `gates` are still treated as enabled.
- All three gates (`annotation-rollout`, `config-rollout`, `sticky-rollout`) have their rollout percentage configured in YAML. The `@EndpointGate` annotation does not have a `rollout` attribute -- rollout is always specified via `endpoint-gate.gates.<name>.rollout`.

### application-sticky.yml

Activating the `sticky` profile registers `UserIdContextResolver` as the `EndpointGateContextResolver` bean. This replaces the default `RandomEndpointGateContextResolver` and enables deterministic rollout based on user IDs.

## How Rollout Works

- **`DefaultRolloutStrategy`** uses SHA-256 hash bucketing: `hash(gateId:userIdentifier) % 100`.
- The same input always produces the same result, enabling sticky rollout.
- **`RandomEndpointGateContextResolver`** (default) generates a random UUID per request, making rollout non-sticky.
- **`UserIdContextResolver`** (sticky profile) extracts the `userId` query parameter, making rollout deterministic per user.
