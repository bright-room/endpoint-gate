# webmvc/actuator-endpoint

This example demonstrates runtime endpoint gate management via the Spring Boot Actuator endpoint.

## Features

- `GET /actuator/endpoint-gates` — list all endpoint gates
- `GET /actuator/endpoint-gates/{gateId}` — get a single endpoint gate
- `POST /actuator/endpoint-gates` — update an endpoint gate (enabled state, optional rollout, and optional schedule)
- `DELETE /actuator/endpoint-gates/{gateId}` — remove an endpoint gate
- `EndpointGateChangedEvent` / `EndpointGateRemovedEvent` / `EndpointGateScheduleChangedEvent` — `@EventListener` handling

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
- `EndpointGateScheduleChangedEvent`: `gateId()`, `schedule()` (null when schedule is removed)

## Demo steps

1. Start the application
2. List all gates: `GET /actuator/endpoint-gates`
3. Get a single gate: `GET /actuator/endpoint-gates/demo-feature`
4. Enable another-feature: `POST /actuator/endpoint-gates` with body `{"gateId":"another-feature","enabled":true}`
5. Delete another-feature: `DELETE /actuator/endpoint-gates/another-feature`
6. Set a schedule on demo-feature (see [Schedule management](#schedule-management))
7. Check the console log for published events

## Schedule management

The `POST /actuator/endpoint-gates` endpoint also accepts schedule fields: `scheduleStart`, `scheduleEnd`, `scheduleTimezone`, and `removeSchedule`.

### Set a schedule

```bash
curl -X POST http://localhost:8080/actuator/endpoint-gates \
  -H "Content-Type: application/json" \
  -d '{
    "gateId": "demo-feature",
    "enabled": true,
    "scheduleStart": "2026-03-10T09:00:00",
    "scheduleEnd": "2026-03-10T18:00:00",
    "scheduleTimezone": "Asia/Tokyo"
  }'
```

### Update a schedule

Setting a schedule is a **full replacement**, not a partial update. Provide all three schedule fields when updating.

```bash
curl -X POST http://localhost:8080/actuator/endpoint-gates \
  -H "Content-Type: application/json" \
  -d '{
    "gateId": "demo-feature",
    "enabled": true,
    "scheduleStart": "2026-04-01T00:00:00",
    "scheduleEnd": "2026-04-30T23:59:59",
    "scheduleTimezone": "Asia/Tokyo"
  }'
```

### Remove a schedule

```bash
curl -X POST http://localhost:8080/actuator/endpoint-gates \
  -H "Content-Type: application/json" \
  -d '{
    "gateId": "demo-feature",
    "enabled": true,
    "removeSchedule": true
  }'
```

### GET response with schedule

```bash
curl http://localhost:8080/actuator/endpoint-gates
```

```json
{
  "gates": [
    {
      "gateId": "demo-feature",
      "enabled": true,
      "rollout": 80,
      "condition": null,
      "schedule": {
        "start": "2026-03-10T09:00:00",
        "end": "2026-03-10T18:00:00",
        "timezone": "Asia/Tokyo",
        "active": true
      }
    }
  ]
}
```

### Notes

- Requests outside the schedule window receive `503 Service Unavailable` with a `Retry-After` header.
- Both setting and removing a schedule publish `EndpointGateScheduleChangedEvent`. When removing, `schedule()` returns `null`.
- Specifying only `scheduleTimezone` without `scheduleStart` or `scheduleEnd` results in a `400 Bad Request`.
