# webmvc/actuator-endpoint

This example demonstrates runtime endpoint gate management via the Spring Boot Actuator endpoint.

## Features

- `GET /actuator/endpoint-gates` — list all endpoint gates
- `GET /actuator/endpoint-gates/{gateId}` — get a single endpoint gate
- `POST /actuator/endpoint-gates` — update an endpoint gate (enabled state and optional rollout)
- `DELETE /actuator/endpoint-gates/{gateId}` — remove an endpoint gate
- `EndpointGateChangedEvent` / `EndpointGateRemovedEvent` — `@EventListener` handling

## Configuration

```yaml
endpoint-gate:
  gates:
    demo-feature:
      enabled: true
      rollout: 80
    another-feature:
      enabled: false

management:
  endpoints:
    web:
      exposure:
        include: endpoint-gates,health
```

## How it works

The `actuator` module automatically registers `MutableInMemoryEndpointGateProvider` and
`MutableInMemoryRolloutPercentageProvider`, which replace the default read-only providers
from the `webmvc` module via `@ConditionalOnMissingBean`.

The `EndpointGateEndpoint` is exposed at `/actuator/endpoint-gates` and allows full CRUD
operations on endpoint gates at runtime without restarting the application.

Events are published on every write or delete:
- `EndpointGateChangedEvent`: `gateId()`, `enabled()`, `rolloutPercentage()` (null if not changed)
- `EndpointGateRemovedEvent`: `gateId()` (only fired when the gate actually existed)

## Demo steps

1. Start the application
2. List all gates: `GET /actuator/endpoint-gates`
3. Get a single gate: `GET /actuator/endpoint-gates/demo-feature`
4. Enable another-feature: `POST /actuator/endpoint-gates` with body `{"gateId":"another-feature","enabled":true}`
5. Delete another-feature: `DELETE /actuator/endpoint-gates/another-feature`
6. Check the console log for published events
