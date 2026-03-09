# webflux/health-indicator

This example demonstrates the `EndpointGateHealthIndicator` which exposes endpoint gate state in the Spring Boot Actuator health endpoint in a Spring WebFlux application.

## Features

- `GET /actuator/health` -- health check with endpoint gate details
- `GET /actuator/health/endpointGate` -- endpoint gate-specific health

## Configuration

```yaml
endpoint-gate:
  gates:
    active-feature:
      enabled: true
    inactive-feature:
      enabled: false
    rollout-feature:
      enabled: true
      rollout: 50

management:
  endpoints:
    web:
      exposure:
        include: health,endpoint-gates
  endpoint:
    health:
      show-details: always
```

## Endpoints

| Endpoint | Endpoint Gate | Enabled | Expected Behavior |
|---|---|---|---|
| `GET /api/active` | `active-feature` | `true` | 200 -- returns response |
| `GET /api/inactive` | `inactive-feature` | `false` | Blocked by endpoint gate |

## Expected health response

```json
{
  "status": "UP",
  "components": {
    "endpointGate": {
      "status": "UP",
      "details": {
        "provider": "MutableInMemoryReactiveEndpointGateProvider",
        "totalGates": 3,
        "enabledGates": 2,
        "disabledGates": 1,
        "defaultEnabled": false
      }
    }
  }
}
```
