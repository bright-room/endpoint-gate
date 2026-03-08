# custom-provider-simple

## Overview

This module demonstrates how to implement custom `EndpointGateProvider` beans and switch between them using Spring Profiles. Two providers are included: one that reads from external configuration properties, and another that reads from environment variables.

## What This Example Demonstrates

- **External configuration provider** -- Implementing `EndpointGateProvider` to read gate values from custom `@ConfigurationProperties` (`ExternalConfigEndpointGateProvider`, active with the `external-config` profile).
- **Environment variable provider** -- Implementing `EndpointGateProvider` to read gate values from environment variables with an `EG_` prefix (`EnvironmentVariableEndpointGateProvider`, active with the `env-variable` profile).
- **Profile-based provider switching** -- Using `@Profile` to activate exactly one provider implementation depending on the active Spring profile.

## How to Run

### External Configuration Profile

```bash
./gradlew :examples:webmvc:custom-provider-simple:bootRun --args='--spring.profiles.active=external-config'
```

### Environment Variable Profile

```bash
EG_DARK_MODE=true ./gradlew :examples:webmvc:custom-provider-simple:bootRun --args='--spring.profiles.active=env-variable'
```

Set the `EG_DARK_MODE` environment variable to `true` before starting the application to enable the `dark-mode` endpoint gate.

## Endpoints

### external-config Profile

| Endpoint | Endpoint Gate | Gate Value | Expected Behavior |
|---|---|---|---|
| `GET /api/cloud` | `cloud-feature` | `true` | 200 -- returns response |
| `GET /api/beta` | `beta-feature` | `false` | Blocked by endpoint gate |

### env-variable Profile

| Endpoint | Endpoint Gate | Environment Variable | Expected Behavior |
|---|---|---|---|
| `GET /ui/dark-mode` | `dark-mode` | `EG_DARK_MODE` | 200 if `EG_DARK_MODE=true`, otherwise blocked |

## Configuration

### application-external-config.yml

The `external-config` profile defines endpoint gate values under a custom configuration prefix:

```yaml
external-endpoint-gates:
  gates:
    cloud-feature: true
    beta-feature: false
```

`ExternalConfigProperties` binds to the `external-endpoint-gates` prefix via `@ConfigurationProperties` and provides the gate map to `ExternalConfigEndpointGateProvider`.

### Environment Variable Naming Convention

`EnvironmentVariableEndpointGateProvider` converts the endpoint gate name to an environment variable key using the following rule:

1. Add the `EG_` prefix.
2. Convert to uppercase.
3. Replace hyphens (`-`) with underscores (`_`).

| Endpoint Gate | Environment Variable |
|---|---|
| `dark-mode` | `EG_DARK_MODE` |
