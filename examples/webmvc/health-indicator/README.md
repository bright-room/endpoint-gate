# webmvc/health-indicator

This example demonstrates the `EndpointGateHealthIndicator` which exposes endpoint gate state in the Spring Boot Actuator health endpoint.

## Features

- `GET /actuator/health` — health check with endpoint gate details
- `GET /actuator/health/endpointGate` — endpoint gate-specific health

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

## Expected health response

```json
{
  "status": "UP",
  "components": {
    "endpointGate": {
      "status": "UP",
      "details": {
        "provider": "MutableInMemoryEndpointGateProvider",
        "totalGates": 3,
        "enabledGates": 2,
        "disabledGates": 1,
        "defaultEnabled": false
      }
    }
  }
}
```
