# Fail Behavior Example

## Overview

This module demonstrates the **fail-closed** (default) and **fail-open** behavior of `endpoint-gate-spring-boot-starter` when an endpoint gate is not explicitly defined in the configuration.

- **Fail-closed** (default): Undefined endpoint gates are treated as disabled. Access to endpoints guarded by unknown gates is blocked.
- **Fail-open**: Undefined endpoint gates are treated as enabled. Access is allowed unless the gate is explicitly set to `false`.

## What This Example Demonstrates

- How undefined endpoint gates behave under the default `fail-closed` policy (`default-enabled: false`).
- How to switch to a `fail-open` policy by setting `default-enabled: true`.
- The difference between explicitly defined gates and undefined gates under each policy.

## How to Run

### Fail-closed (default)

```shell
./gradlew :examples:webmvc:fail-behavior:bootRun
```

### Fail-open

```shell
./gradlew :examples:webmvc:fail-behavior:bootRun --args='--spring.profiles.active=fail-open'
```

## Endpoints

### FailClosedController (default profile)

| Endpoint              | Endpoint Gate        | Defined | Expected Result             |
|-----------------------|----------------------|---------|-----------------------------|
| `GET /fail-closed/known`   | `known-feature`      | Yes (`true`)  | 200 OK -- access allowed   |
| `GET /fail-closed/unknown` | `undefined-feature`  | No      | Blocked -- gate is undefined and `default-enabled` is `false` |

### FailOpenController (fail-open profile)

| Endpoint                     | Endpoint Gate        | Defined | Expected Result             |
|------------------------------|----------------------|---------|-----------------------------|
| `GET /fail-open/known-disabled` | `known-disabled`     | Yes (`false`) | Blocked -- explicitly disabled |
| `GET /fail-open/unknown`        | `undefined-feature`  | No      | 200 OK -- gate is undefined but `default-enabled` is `true`  |

## Configuration

### Default profile (`application.yml`)

```yaml
endpoint-gate:
  gates:
    known-feature:
      enabled: true
```

`default-enabled` is `false` by default, so any endpoint gate not listed under `gates` will be treated as disabled.

### Fail-open profile (`application-fail-open.yml`)

```yaml
endpoint-gate:
  gates:
    known-disabled:
      enabled: false
  default-enabled: true
```

Setting `default-enabled: true` means any endpoint gate not listed under `gates` will be treated as enabled.
