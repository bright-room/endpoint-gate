# functional-endpoint

## Overview

This module demonstrates how to use `endpoint-gate-spring-boot-starter` with Spring MVC functional endpoints (`RouterFunction`), using `EndpointGateHandlerFilterFunction` to gate access to routes.

## What This Example Demonstrates

- **Full gating** -- Using `endpointGateFilter.of("gate-id")` to completely gate a route behind an endpoint gate (`stableRoute`, `experimentalRoute`).
- **Rollout gating** -- Using `endpointGateFilter.of("gate-id", 50)` to enable a route for a percentage of requests (`betaRoute`).
- **RouterFunction integration** -- Applying endpoint gate filters within `RouterFunctions.route()` builder chains (`RoutingConfiguration`).

## How to Run

```bash
./gradlew :examples:webmvc:functional-endpoint:bootRun
```

## Endpoints

| Endpoint | Endpoint Gate | Rollout | Gate Value | Expected Behavior |
|---|---|---|---|---|
| `GET /api/stable` | `stable-api` | 100% | `true` | 200 -- returns response |
| `GET /api/beta` | `beta-api` | 50% | `true` | ~50% of requests succeed (random) |
| `GET /api/experimental` | `experimental-api` | 100% | `false` | Blocked by endpoint gate |

## Configuration

### application.yml

```yaml
endpoint-gate:
  gates:
    stable-api:
      enabled: true
    beta-api:
      enabled: true
    experimental-api:
      enabled: false
```

## Key Differences from Annotation-based Approach

- Endpoint gates are applied via `EndpointGateHandlerFilterFunction` as a `RouterFunction` filter, not via `@EndpointGate` annotations.
- The webmvc `EndpointGateHandlerFilterFunction` does **not** use `RolloutPercentageProvider` -- configuration-based rollout values (`endpoint-gate.gates.<name>.rollout`) are not applied to functional endpoints.
- Rollout percentage can only be specified through the `of(gateId, rollout)` parameter.
