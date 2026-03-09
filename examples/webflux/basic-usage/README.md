# webflux/basic-usage

## Overview

This module demonstrates the basic usage of `endpoint-gate` with Spring WebFlux, covering annotation placement patterns for reactive controllers returning `Mono` and `Flux`.

## What This Example Demonstrates

- **Class-level annotation** -- Applying `@EndpointGate` to a controller class so that all endpoints in the class are controlled by a single flag (`GreetingController`).
- **Method-level annotation** -- Applying `@EndpointGate` to individual handler methods, allowing per-endpoint control. Endpoints without the annotation remain always accessible (`UserController`).
- **Annotation priority** -- When both class-level and method-level `@EndpointGate` are present, the method-level annotation takes priority (`LegacyController`).
- **Reactive return types** -- Feature flag evaluation works transparently with `Mono<T>` and `Flux<T>` return types.

## How to Run

```bash
./gradlew :webflux:basic-usage:bootRun
```

## Endpoints

| Endpoint | Annotation | Endpoint Gate | Flag Value | Expected Behavior |
|---|---|---|---|---|
| `GET /hello` | Class-level | `greeting` | `true` | 200 -- returns response |
| `GET /hello/stream` | Class-level | `greeting` | `true` | 200 -- returns streaming response |
| `GET /users/search` | Method-level | `new-search` | `true` | 200 -- returns response |
| `GET /users/export` | Method-level | `new-export` | `false` | Blocked by endpoint gate |
| `GET /users/list` | None | -- | -- | 200 -- always accessible |
| `GET /legacy/data` | Class-level | `legacy-api` | `false` | Blocked by endpoint gate |
| `GET /legacy/special` | Class-level + Method-level | `special-endpoint` | `true` | 200 -- method-level flag takes priority |

## Configuration

### application.yml

```yaml
endpoint-gates:
  features:
    greeting:
      enabled: true
    new-search:
      enabled: true
    new-export:
      enabled: false
    legacy-api:
      enabled: false
    special-endpoint:
      enabled: true
```
