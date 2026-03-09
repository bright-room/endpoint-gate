# webflux/rollout

## Overview

This module demonstrates the gradual rollout feature of `endpoint-gate` with Spring WebFlux, covering annotation-based rollout, configuration-based rollout, and sticky rollout using a custom `ReactiveEndpointGateContextResolver`.

## What This Example Demonstrates

- **Annotation-based rollout** -- Using `@EndpointGate(value = "feature-name", rollout = 50)` to enable a feature for a percentage of requests (`AnnotationRolloutController`).
- **Configuration-based rollout** -- Specifying rollout percentage via `endpoint-gates.features.<name>.rollout` in the configuration file (`ConfigRolloutController`).
- **Sticky rollout** -- Implementing a custom `ReactiveEndpointGateContextResolver` that uses a user ID for deterministic rollout decisions (`StickyRolloutController`, `UserIdReactiveContextResolver`).
- **Rollout priority** -- Configuration-based rollout takes priority over annotation-based rollout. When a feature is defined in `features`, the `RolloutPercentageProvider` value is used and the annotation's `rollout` attribute is ignored.

## How to Run

Start the application with the default profile (random, non-sticky rollout):

```bash
./gradlew :webflux:rollout:bootRun
```

To enable sticky rollout using user IDs:

```bash
./gradlew :webflux:rollout:bootRun --args='--spring.profiles.active=sticky'
```

> **Note:** The `ReactiveEndpointGateContextResolver` is a single application-wide bean. Activating the `sticky` profile affects all rollout endpoints, not just the sticky-rollout endpoint.

## Endpoints

| Endpoint | Endpoint Gate | Rollout | Source | Expected Behavior |
|---|---|---|---|---|
| `GET /api/annotation-rollout` | `annotation-rollout` | 50% | Annotation | ~50% of requests succeed (random) |
| `GET /api/config-rollout` | `config-rollout` | 30% | Configuration | ~30% of requests succeed (random) |
| `GET /api/sticky-rollout` | `sticky-rollout` | 50% | Annotation | ~50% of users succeed (deterministic with `sticky` profile) |

With the `sticky` profile, pass a `userId` query parameter to get deterministic results:

```bash
curl http://localhost:8080/api/sticky-rollout?userId=user-123
```

The same `userId` will always produce the same result for the same feature.

## Configuration

### application.yml

```yaml
endpoint-gates:
  default-enabled: true
  features:
    config-rollout:
      enabled: true
      rollout: 30
```

- `default-enabled: true` enables fail-open behavior, so features not listed in `features` (like `annotation-rollout` and `sticky-rollout`) are still treated as enabled.
- `annotation-rollout` is intentionally **not** defined in `features` -- this allows the annotation's `rollout = 50` attribute to take effect. If it were defined, the configuration's default rollout of `100` would override the annotation value.

### application-sticky.yml

Activating the `sticky` profile registers `UserIdReactiveContextResolver` as the `ReactiveEndpointGateContextResolver` bean. This replaces the default resolver and enables deterministic rollout based on user IDs extracted from the `userId` query parameter.

## How Rollout Works

- **`DefaultRolloutStrategy`** uses SHA-256 hash bucketing: `hash(gateId:userIdentifier) % 100`.
- The same input always produces the same result, enabling sticky rollout.
- **Default resolver** (no profile) generates a random UUID per request, making rollout non-sticky.
- **`UserIdReactiveContextResolver`** (`sticky` profile) extracts the `userId` query parameter, making rollout deterministic per user.
