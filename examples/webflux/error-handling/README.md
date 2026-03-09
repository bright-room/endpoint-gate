# webflux/error-handling

## Overview

This module demonstrates how to customize error handling when a endpoint gate denies access in a Spring WebFlux application. By default, `endpoint-gate` throws a `EndpointGateAccessDeniedException`. This example shows three approaches to handling that exception, switched via Spring profiles.

- **JSON error response** (`json-error` profile): Returns a structured 403 JSON response via `@ControllerAdvice`.
- **Redirect** (`redirect` profile): Redirects the client to `/api/coming-soon` via `@ControllerAdvice`.
- **Functional endpoint error resolution** (`functional-error` profile): Returns a 403 JSON response via a custom `AccessDeniedHandlerFilterResolution` for functional endpoints.

## What This Example Demonstrates

- How to implement a `@ControllerAdvice` that catches `EndpointGateAccessDeniedException` and returns a JSON error response.
- How to implement a `@ControllerAdvice` that catches `EndpointGateAccessDeniedException` and redirects to a different page.
- How to implement `AccessDeniedHandlerFilterResolution` to customize the access-denied response for functional endpoints using `EndpointGateHandlerFilterFunction`.
- How to use Spring `@Profile` to switch between different error handling strategies.

## How to Run

### JSON error response

```bash
./gradlew :webflux:error-handling:bootRun --args='--spring.profiles.active=json-error'
```

### Redirect

```bash
./gradlew :webflux:error-handling:bootRun --args='--spring.profiles.active=redirect'
```

### Functional endpoint error resolution

```bash
./gradlew :webflux:error-handling:bootRun --args='--spring.profiles.active=functional-error'
```

## Endpoints

### PremiumController (annotation-based)

| Endpoint | Endpoint Gate | Flag Value | Expected Result |
|---|---|---|---|
| `GET /api/premium` | `premium-feature` | `false` | Blocked -- handled by the active error handler |
| `GET /api/coming-soon` | (none) | -- | 200 OK -- always accessible (redirect target) |

### Functional route (`functional-error` profile only)

| Endpoint | Endpoint Gate | Flag Value | Expected Result |
|---|---|---|---|
| `GET /functional/premium` | `premium-feature` | `false` | Blocked -- handled by `CustomHandlerFilterResolution` |

### Error Handling Behavior by Profile

| Profile | Handler | Behavior |
|---|---|---|
| `json-error` | `CustomExceptionHandler` (`@ControllerAdvice`) | Returns 403 JSON with `error`, `feature`, and `message` fields |
| `redirect` | `RedirectExceptionHandler` (`@ControllerAdvice`) | Redirects (302) to `/api/coming-soon` |
| `functional-error` | `CustomHandlerFilterResolution` | Returns 403 JSON for the functional route via `AccessDeniedHandlerFilterResolution` |

## Configuration

### application.yml

```yaml
endpoint-gates:
  features:
    premium-feature:
      enabled: false
```

`premium-feature` is set to `false`, so all guarded endpoints will throw `EndpointGateAccessDeniedException`. The active error handler (determined by the Spring profile) decides how to respond.
