# webflux/fail-behavior

## Overview

This module demonstrates the **fail-closed** (default) and **fail-open** behavior of `endpoint-gate` in a Spring WebFlux application when a endpoint gate is not explicitly defined in the configuration.

- **Fail-closed** (default): Undefined endpoint gates are treated as disabled. Access to endpoints guarded by unknown flags is blocked.
- **Fail-open**: Undefined endpoint gates are treated as enabled. Access is allowed unless the flag is explicitly set to `false`.

## What This Example Demonstrates

- How undefined endpoint gates behave under the default `fail-closed` policy (`default-enabled: false`).
- How to switch to a `fail-open` policy by setting `default-enabled: true`.
- The difference between explicitly defined flags and undefined flags under each policy.

## How to Run

### Fail-closed (default)

```bash
./gradlew :webflux:fail-behavior:bootRun
```

### Fail-open

```bash
./gradlew :webflux:fail-behavior:bootRun --args='--spring.profiles.active=fail-open'
```

## Endpoints

### FailClosedController (default profile)

| Endpoint | Endpoint Gate | Defined | Expected Result |
|---|---|---|---|
| `GET /fail-closed/known` | `known-feature` | Yes (`true`) | 200 OK -- access allowed |
| `GET /fail-closed/unknown` | `undefined-feature` | No | Blocked -- flag is undefined and `default-enabled` is `false` |

### FailOpenController (fail-open profile)

| Endpoint | Endpoint Gate | Defined | Expected Result |
|---|---|---|---|
| `GET /fail-open/known-disabled` | `known-disabled` | Yes (`false`) | Blocked -- explicitly disabled |
| `GET /fail-open/unknown` | `undefined-feature` | No | 200 OK -- flag is undefined but `default-enabled` is `true` |

## Configuration

### Default profile (`application.yml`)

```yaml
endpoint-gates:
  features:
    known-feature:
      enabled: true
    known-disabled:
      enabled: false
```

`default-enabled` is `false` by default, so any endpoint gate not listed under `features` will be treated as disabled.

### Fail-open profile (`application-fail-open.yml`)

```yaml
endpoint-gates:
  features:
    known-disabled:
      enabled: false
  default-enabled: true
```

Setting `default-enabled: true` means any endpoint gate not listed under `features` will be treated as enabled.
