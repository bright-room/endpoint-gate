# custom-rollout-strategy

## Overview

This module demonstrates how to implement custom `RolloutStrategy` beans and switch between them using Spring Profiles. Two custom strategies are included: one that gates access by the region specified in a request header, and one that restricts rollout to business hours.

## What This Example Demonstrates

- **Region-based rollout** -- Implementing `RolloutStrategy` to determine rollout eligibility by the `X-Region` request header, with a pre-defined weight per region (`RegionBasedRolloutStrategy`, active with the `region` profile).
- **Time-based rollout** -- Implementing `RolloutStrategy` to allow rollout only during business hours (09:00-17:00), and delegating to `DefaultRolloutStrategy` within that window (`TimeBasedRolloutStrategy`, active with the `time-based` profile).
- **Custom context resolver** -- Implementing `EndpointGateContextResolver` to extract the region identifier from the `X-Region` header and expose it as the `EndpointGateContext` user identifier (`RegionContextResolver`, active with the `region` profile).
- **Profile-based strategy switching** -- Using `@Profile` to activate exactly one `RolloutStrategy` implementation depending on the active Spring profile.

## How to Run

### Default (hash-based rollout)

```bash
./gradlew :examples:webmvc:custom-rollout-strategy:bootRun
```

### Region-based rollout

```bash
./gradlew :examples:webmvc:custom-rollout-strategy:bootRun --args='--spring.profiles.active=region'
```

### Time-based rollout

```bash
./gradlew :examples:webmvc:custom-rollout-strategy:bootRun --args='--spring.profiles.active=time-based'
```

## Endpoints

| Endpoint | Endpoint Gate | Rollout | Expected Behavior |
|---|---|---|---|
| `GET /api/gradual` | `gradual-feature` | 60% | Depends on the active rollout strategy |

## Strategy Behavior

### Default profile

Uses the built-in `DefaultRolloutStrategy` (SHA-256 hash bucketing). Rollout is non-sticky by default because the default `EndpointGateContextResolver` generates a random UUID per request.

### `region` profile

`RegionBasedRolloutStrategy` maps each region to a weight and compares it to the rollout percentage:

| Region | Weight | Result (rollout=60) |
|---|---|---|
| `us-east` | 0 | In rollout (0 < 60) |
| `eu-west` | 50 | In rollout (50 < 60) |
| `ap-northeast` | 80 | Not in rollout (80 >= 60) |
| (other) | 100 | Not in rollout |

Pass the region via the `X-Region` header:

```bash
curl -H "X-Region: us-east" http://localhost:8080/api/gradual
curl -H "X-Region: ap-northeast" http://localhost:8080/api/gradual
```

`RegionContextResolver` reads the `X-Region` header and sets it as the `EndpointGateContext` user identifier consumed by `RegionBasedRolloutStrategy`.

### `time-based` profile

`TimeBasedRolloutStrategy` blocks all access outside of 09:00-17:00 (server local time). Within business hours, it delegates to `DefaultRolloutStrategy` with the configured percentage.

## Configuration

### application.yml

```yaml
endpoint-gate:
  default-enabled: true
  gates:
    gradual-feature:
      enabled: true
      rollout: 60
```
