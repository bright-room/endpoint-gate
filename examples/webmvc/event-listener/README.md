# event-listener

## Overview

This module demonstrates how to react to endpoint gate lifecycle events published by `endpoint-gate-spring-boot-starter`. Two listener beans are included: one that maintains an in-memory audit log, and one that invalidates a local cache whenever a gate changes or is removed.

## What This Example Demonstrates

- **Audit logging** -- Implementing `@EventListener` methods that handle `EndpointGateChangedEvent` and `EndpointGateRemovedEvent` and append timestamped entries to an in-memory audit log (`AuditEventListener`).
- **Cache invalidation** -- Implementing `@EventListener` methods that evict the affected endpoint gate entry from a local `EndpointGateCache` whenever a gate changes or is removed (`CacheInvalidationListener`).
- **Multiple listeners** -- Registering more than one `@EventListener` component for the same event type; both listeners receive each event independently.
- **Actuator integration** -- Exposing the `endpoint-gates` Actuator endpoint to trigger gate changes at runtime and observe the resulting events.

## How to Run

```bash
./gradlew :examples:webmvc:event-listener:bootRun
```

## Endpoints

### Application endpoints

| Endpoint | Description |
|---|---|
| `GET /api/demo` | Endpoint gate controlled endpoint (guarded by `demo-feature`) |
| `GET /api/audit-log` | Returns all entries recorded by `AuditEventListener` |
| `GET /api/cache` | Returns the current contents of `EndpointGateCache` |

### Actuator endpoints

| Endpoint | Description |
|---|---|
| `GET /actuator/endpoint-gates` | List all endpoint gates |
| `POST /actuator/endpoint-gates` | Update an endpoint gate (triggers events) |
| `DELETE /actuator/endpoint-gates/{gateId}` | Remove an endpoint gate (triggers events) |

## Demo Steps

1. Start the application.
2. Access the endpoint gate controlled endpoint: `GET /api/demo`
3. Update the gate via the Actuator:
   ```bash
   curl -X POST http://localhost:8080/actuator/endpoint-gates \
     -H 'Content-Type: application/json' \
     -d '{"gateId":"demo-feature","enabled":false}'
   ```
4. Check the audit log: `GET /api/audit-log`
5. Check the cache state: `GET /api/cache`
6. Remove the gate: `DELETE http://localhost:8080/actuator/endpoint-gates/demo-feature`
7. Check the audit log again to see the removal entry.

## Configuration

### application.yml

```yaml
endpoint-gate:
  gates:
    demo-feature:
      enabled: true

management:
  endpoints:
    web:
      exposure:
        include: endpoint-gates,health
```
