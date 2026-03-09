# webflux/custom-provider

## Overview

This module demonstrates how to implement a custom `ReactiveEndpointGateProvider` for Spring WebFlux. The provider reads endpoint gate values from custom `@ConfigurationProperties` and exposes them as a reactive `Mono<Boolean>`.

## What This Example Demonstrates

- **Custom `ReactiveEndpointGateProvider`** -- Implementing `ReactiveEndpointGateProvider` to load endpoint gate state from custom configuration properties (`ExternalConfigReactiveEndpointGateProvider`).
- **`@ConfigurationProperties` binding** -- Using `ExternalConfigProperties` to bind a `Map<String, Boolean>` under a custom prefix (`external-endpoint-gates.flags`).
- **Reactive flag evaluation** -- Returning `Mono<Boolean>` from the provider, which integrates with the WebFlux endpoint gate interception pipeline.

## How to Run

```bash
./gradlew :webflux:custom-provider:bootRun
```

## Endpoints

| Endpoint | Endpoint Gate | Flag Value | Expected Behavior |
|---|---|---|---|
| `GET /api/reactive-feature` | `reactive-feature` | `true` | 200 -- returns response |
| `GET /api/beta-feature` | `beta-feature` | `false` | Blocked by endpoint gate |

## Configuration

### application.yml

```yaml
external-endpoint-gates:
  flags:
    reactive-feature: true
    beta-feature: false
```

`ExternalConfigProperties` binds to the `external-endpoint-gates` prefix via `@ConfigurationProperties` and provides the flag map to `ExternalConfigReactiveEndpointGateProvider`.

When a requested endpoint gate is not found in the map, the provider returns `Mono.empty()`, which causes the library to fall back to the configured `default-enabled` behavior (fail-closed by default).
