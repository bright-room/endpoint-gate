# endpoint-gate

Endpoint access control for JVM applications — feature flags, conditional access, rollout, and schedules.

## Features

- Endpoint gate functions can be realized with minimal configuration.
- The source destination for gate management can be easily changed.
- Supports both MVC and WebFlux.

## Installation

See the [release notes](https://github.com/bright-room/endpoint-gate/releases) for available versions.

### Apache Maven

```xml
<dependencies>
    <!-- Spring MVC -->
    <dependency>
        <groupId>net.bright-room.endpoint-gate</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>${version}</version>
    </dependency>

    <!-- Spring WebFlux -->
    <dependency>
        <groupId>net.bright-room.endpoint-gate</groupId>
        <artifactId>spring-webflux</artifactId>
        <version>${version}</version>
    </dependency>

    <!-- Runtime gate management via Actuator (optional) -->
    <dependency>
        <groupId>net.bright-room.endpoint-gate</groupId>
        <artifactId>spring-actuator</artifactId>
        <version>${version}</version>
    </dependency>

    <!-- Micrometer metrics for gate evaluations (optional) -->
    <dependency>
        <groupId>net.bright-room.endpoint-gate</groupId>
        <artifactId>spring-metrics</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
```

### Gradle(Groovy)
```groovy
dependencies {
    // Spring MVC
    implementation 'net.bright-room.endpoint-gate:spring-webmvc:${version}'

    // Spring WebFlux
    implementation 'net.bright-room.endpoint-gate:spring-webflux:${version}'

    // Runtime gate management via Actuator (optional)
    implementation 'net.bright-room.endpoint-gate:spring-actuator:${version}'

    // Micrometer metrics for gate evaluations (optional)
    implementation 'net.bright-room.endpoint-gate:spring-metrics:${version}'
}
```

### Gradle(Kotlin)
```kotlin
dependencies {
    // Spring MVC
    implementation("net.bright-room.endpoint-gate:spring-webmvc:${version}")

    // Spring WebFlux
    implementation("net.bright-room.endpoint-gate:spring-webflux:${version}")

    // Runtime gate management via Actuator (optional)
    implementation("net.bright-room.endpoint-gate:spring-actuator:${version}")

    // Micrometer metrics for gate evaluations (optional)
    implementation("net.bright-room.endpoint-gate:spring-metrics:${version}")
}
```

## Configuration

By default, gates are defined in the configuration file.

```yaml
endpoint-gate:
  gates:
    hello-class:
      enabled: true
    user-find:
      enabled: false
      rollout: 50
  default-enabled: false  # false (fail-closed, default) | true (fail-open)
  response:
    type: JSON  # PLAIN_TEXT | JSON | HTML (default: JSON)
```

> **Undefined gates are blocked by default (fail-closed).** If a gate ID referenced in a `@EndpointGate` annotation is not listed under `endpoint-gate.gates`, access is denied with `403 Forbidden`. Set `endpoint-gate.default-enabled: true` to allow access for undefined gates instead (fail-open).

Add the `@EndpointGate` annotation to the class or method that will be the endpoint.

```java

// HelloController.java
@RestController
@EndpointGate("hello-class")
class HelloController {

  @GetMapping("/hello")
  String hello() {
    return "Hello world!!";
  }
}

// UserController.java
@RestController
class UserController {

  UserService userService;

  @GetMapping("/find")
  @EndpointGate("user-find")
  UserResponse find(@RequestParam("name") String name) {
    return userService.find(name);
  }

  UserController(UserService userService) {
    this.userService = userService;
  }
}
```

## Multiple Gates (AND Semantics)

You can specify multiple gate IDs in a single `@EndpointGate` annotation. All specified gates must permit access — if any gate denies access, the request is rejected with `403 Forbidden`.

```java
// All gates must be enabled for the endpoint to be accessible
@GetMapping("/dashboard")
@EndpointGate({"feature-new-dashboard", "beta-users-only"})
String dashboard() {
  return "Welcome to the new dashboard!";
}
```

For functional endpoints, pass multiple gate IDs to `of()`:

```java
// Spring MVC functional endpoint
@Bean
RouterFunction<ServerResponse> routes(EndpointGateHandlerFilterFunction endpointGateFilter) {
    return route()
        .GET("/dashboard", handler::handle)
        .filter(endpointGateFilter.of("feature-new-dashboard", "beta-users-only"))
        .build();
}
```

```java
// Spring WebFlux functional endpoint
@Bean
RouterFunction<ServerResponse> routes(EndpointGateHandlerFilterFunction endpointGateFilter) {
    return route()
        .GET("/dashboard", handler::handle)
        .filter(endpointGateFilter.of("feature-new-dashboard", "beta-users-only"))
        .build();
}
```

The gates are evaluated sequentially. As soon as one gate denies access, evaluation short-circuits and the request is rejected.

## Change the source destination for gate management

By default, gate management can be set in the configuration file, but it is also possible to change the source destination.

By changing the source of gate management to a database, external file, etc., it is possible to control in real time.

To change the source destination, simply implement the `EndpointGateProvider` (Spring MVC) or `ReactiveEndpointGateProvider` (Spring WebFlux) and register the bean.

### Spring MVC

```java

// EndpointGateExternalDataSourceProvider.java
@Component
class EndpointGateExternalDataSourceProvider implements EndpointGateProvider {

  GateManagementMapper gateManagementMapper;

  @Override
  public boolean isGateEnabled(String gateId) {
    Boolean enabled = gateManagementMapper.check(gateId);
    // Choose your undefined-gate policy:
    //   return false; — fail-closed: block access for undefined gates (recommended)
    //   return true; — fail-open: allow access for undefined gates
    if (enabled == null) return false;
    return enabled;
  }

  EndpointGateExternalDataSourceProvider(GateManagementMapper gateManagementMapper) {
    this.gateManagementMapper = gateManagementMapper;
  }
}

// GateManagementMapper.java
@Mapper
interface GateManagementMapper {
  Boolean check(@Param("gateId") String gateId);
}
```

### Spring WebFlux

```java
@Component
class ReactiveEndpointGateExternalDataSourceProvider implements ReactiveEndpointGateProvider {

  GateManagementRepository gateManagementRepository;

  @Override
  public Mono<Boolean> isGateEnabled(String gateId) {
    return gateManagementRepository.findByGateId(gateId)
        .map(GateManagement::enabled)
        // Choose your undefined-gate policy:
        //   Mono.just(false) — fail-closed (recommended)
        //   Mono.just(true) — fail-open
        .defaultIfEmpty(false);
  }

  ReactiveEndpointGateExternalDataSourceProvider(GateManagementRepository gateManagementRepository) {
    this.gateManagementRepository = gateManagementRepository;
  }
}
```

## Response Types

When a gate is disabled, `EndpointGateAccessDeniedException` is thrown and the response is returned with HTTP status `403 Forbidden`. The response format is selected by `endpoint-gate.response.type`.

### JSON Response (default)

JSON responses follow the [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807) format.

```yaml
endpoint-gate:
  response:
    type: JSON
```

Response body:

```json
{
  "type": "https://github.com/bright-room/endpoint-gate#response-types",
  "title": "Endpoint gate access denied",
  "detail": "Gate 'user-find' is not available",
  "status": 403,
  "instance": "/api/v2/find"
}
```

### Plain Text Response

```yaml
endpoint-gate:
  response:
    type: PLAIN_TEXT
```

Response body:

```
Gate 'user-find' is not available
```

### HTML Response

```yaml
endpoint-gate:
  response:
    type: HTML
```

> **Note (Spring MVC only):** The HTML response is returned only when the client's `Accept` header includes `text/html` or `text/*`. If the client only accepts `application/json`, a `406 Not Acceptable` response is returned instead. In Spring WebFlux, the HTML response is always returned regardless of the `Accept` header.

## Custom Access Denied Response

### Spring MVC

You can create a fully custom response by defining a `@ControllerAdvice` that handles `EndpointGateAccessDeniedException`. It takes priority over the library's default handler.

```java
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// CustomEndpointGateExceptionHandler.java
@ControllerAdvice
@Order(0) // Ensure this handler takes priority over the library's default handler
public class CustomEndpointGateExceptionHandler {

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  public ResponseEntity<String> handle(EndpointGateAccessDeniedException e) {
    return ResponseEntity.status(403)
        .contentType(MediaType.TEXT_PLAIN)
        .body("Gate '" + e.gateId() + "' is disabled.");
  }
}
```

### Spring WebFlux (Annotation-based controllers)

You can create a fully custom response by defining a `@ControllerAdvice` that handles `EndpointGateAccessDeniedException`. It takes priority over the library's default handler.

```java
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// CustomEndpointGateExceptionHandler.java
@ControllerAdvice
@Order(0) // Ensure this handler takes priority over the library's default handler
public class CustomEndpointGateExceptionHandler {

  @ExceptionHandler(EndpointGateAccessDeniedException.class)
  ResponseEntity<String> handle(EndpointGateAccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body("Gate '" + e.gateId() + "' is disabled.");
  }
}
```

### Spring WebFlux (Functional endpoints)

Define an `AccessDeniedHandlerFilterResolution` bean to customize the response returned by the `HandlerFilterFunction`.

```java
import net.brightroom.endpointgate.core.exception.EndpointGateAccessDeniedException;
import net.brightroom.endpointgate.spring.webflux.resolution.handlerfilter.AccessDeniedHandlerFilterResolution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

// CustomHandlerFilterResolutionConfig.java
@Configuration
public class CustomHandlerFilterResolutionConfig {

  @Bean
  AccessDeniedHandlerFilterResolution customResolution() {
    return (request, e) -> ServerResponse.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.TEXT_PLAIN)
        .bodyValue("Gate '" + e.gateId() + "' is disabled.");
  }
}
```

## Conditional Endpoint Gates

Use the `condition` property in `application.yml` to enable a gate only when a SpEL expression
evaluated against the incoming request is satisfied.

```yaml
endpoint-gate:
  gates:
    new-feature:
      enabled: true
      condition: "headers['X-Beta'] != null"
```

```java
@RestController
class BetaController {

  @GetMapping("/new-feature")
  @EndpointGate("new-feature")
  String newFeature() {
    return "You're in the beta!";
  }
}
```

The evaluation order is: **gate enabled** → **schedule** → **condition** → **rollout**. If the condition is not
satisfied, `403 Forbidden` is returned.

### Available Variables

| Variable | Type | Description |
|---|---|---|
| `headers` | `Map<String, String>` | Request headers (first value per name) |
| `params` | `Map<String, String>` | Query parameters (first value per name) |
| `cookies` | `Map<String, String>` | Cookie values keyed by name |
| `path` | `String` | Request path (e.g. `/api/resource`) |
| `method` | `String` | HTTP method (e.g. `GET`, `POST`) |
| `remoteAddress` | `String` | Client IP address |

### Configuration

By default, if the SpEL expression throws an error, access is denied (fail-closed). Set
`endpoint-gate.condition.fail-on-error: false` to allow access instead (fail-open).

```yaml
endpoint-gate:
  condition:
    fail-on-error: true  # true (fail-closed, default) | false (fail-open)
```

### WebFlux Functional Endpoints

Pass a condition expression as the second argument to `of`:

```java
@Bean
RouterFunction<ServerResponse> routes(EndpointGateHandlerFilterFunction endpointGateFilter) {
    return route()
        .GET("/new-feature", handler::handle)
        .filter(endpointGateFilter.of("new-feature", "headers['X-Beta'] != null"))
        .build();
}
```

You can also combine condition and rollout:

```java
.filter(endpointGateFilter.of("new-feature", "headers['X-Beta'] != null", 50))
```

### Custom Condition Evaluator

Implement `EndpointGateConditionEvaluator` (Spring MVC) or `ReactiveEndpointGateConditionEvaluator`
(Spring WebFlux) and register it as a `@Bean` to replace the default SpEL-based evaluator.

```java
// Spring MVC
@Component
class CustomConditionEvaluator implements EndpointGateConditionEvaluator {

  @Override
  public boolean evaluate(String expression, ConditionVariables variables) {
    // custom evaluation logic
    return true;
  }
}
```

```java
// Spring WebFlux
@Component
class CustomReactiveConditionEvaluator implements ReactiveEndpointGateConditionEvaluator {

  @Override
  public Mono<Boolean> evaluate(String expression, ConditionVariables variables) {
    // non-blocking evaluation logic
    return Mono.just(true);
  }
}
```

## Gradual Rollout

Use the `rollout` property in `application.yml` to enable a gate for only a percentage of requests.

```yaml
endpoint-gate:
  gates:
    new-feature:
      enabled: true
      rollout: 50  # enable for 50% of requests (0-100, default: 100)
```

```java
@RestController
class BetaController {

  @GetMapping("/new-feature")
  @EndpointGate("new-feature")
  String newFeature() {
    return "You're in the rollout!";
  }
}
```

When `endpoint-gate.gates.*.rollout` is configured, it is used for the rollout percentage. The config value can also be overridden at runtime via the [Actuator endpoint](#runtime-gate-management-actuator).

By default, rollout is **non-sticky** — each request is evaluated independently using a random identifier. This means the same user may see different behavior across requests.

### Sticky Rollout

To make rollout sticky (the same user always gets the same result), implement `EndpointGateContextResolver` (Spring MVC) or `ReactiveEndpointGateContextResolver` (Spring WebFlux) and register it as a `@Bean`.

```java
// Spring MVC
@Component
class UserBasedContextResolver implements EndpointGateContextResolver {

  @Override
  public Optional<EndpointGateContext> resolve(HttpServletRequest request) {
    String userId = request.getHeader("X-User-Id");
    if (userId == null) return Optional.empty(); // fail-open: skip rollout check
    return Optional.of(new EndpointGateContext(userId));
  }
}
```

```java
// Spring WebFlux
@Component
class UserBasedReactiveContextResolver implements ReactiveEndpointGateContextResolver {

  @Override
  public Mono<EndpointGateContext> resolve(ServerHttpRequest request) {
    String userId = request.getHeaders().getFirst("X-User-Id");
    if (userId == null) return Mono.empty(); // fail-open: skip rollout check
    return Mono.just(new EndpointGateContext(userId));
  }
}
```

When the context resolver returns empty, the rollout check is skipped and the gate is treated as fully enabled (fail-open).

### Custom Rollout Strategy

To change how the rollout bucketing works, implement `RolloutStrategy` (Spring MVC) or `ReactiveRolloutStrategy` (Spring WebFlux) and register it as a `@Bean`.

### WebFlux Functional Endpoints

For functional endpoints, use `EndpointGateHandlerFilterFunction.of(name, rollout)`:

```java
@Bean
RouterFunction<ServerResponse> routes(EndpointGateHandlerFilterFunction endpointGateFilter) {
    return route()
        .GET("/new-feature", handler::handle)
        .filter(endpointGateFilter.of("new-feature", 50))
        .build();
}
```

## Runtime Gate Management (Actuator)

The `spring-actuator` module provides a Spring Boot Actuator endpoint for reading and updating gates at runtime without restarting the application.

### Setup

1. Add the `spring-actuator` dependency (see [Installation](#installation)).
2. Expose the endpoint(s):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: endpoint-gates, health
```

### Read all gates

```
GET /actuator/endpoint-gates
```

Response:

```json
{
  "gates": [
    { "gateId": "hello-class", "enabled": true, "rollout": 100 },
    { "gateId": "user-find", "enabled": false, "rollout": 50 }
  ],
  "defaultEnabled": false
}
```

### Read a single gate

```
GET /actuator/endpoint-gates/{gateId}
```

Response:

```json
{
  "gateId": "user-find",
  "enabled": false,
  "rollout": 100
}
```

If the gate is not defined, `enabled` reflects the `defaultEnabled` policy and `rollout` is `100`.

### Update a gate

```
POST /actuator/endpoint-gates
Content-Type: application/json

{
  "gateId": "user-find",
  "enabled": true,
  "rollout": 50
}
```

The `rollout` field is optional (0-100). If omitted, the rollout percentage is left unchanged.

Response:

```json
{
  "gates": [
    { "gateId": "hello-class", "enabled": true, "rollout": 100 },
    { "gateId": "user-find", "enabled": true, "rollout": 50 }
  ],
  "defaultEnabled": false
}
```

If the gate does not exist, it is created with the given state.

### Delete a gate

```
DELETE /actuator/endpoint-gates/{gateId}
```

Removes the gate and its associated rollout percentage. Returns `204 No Content`.

This operation is idempotent: deleting a non-existent gate is a no-op and returns `204 No Content` without publishing any event.

### Restricting access

By default, both read and write operations are unrestricted. In production, consider restricting access:

```yaml
management:
  endpoint:
    endpoint-gates:
      access: READ_ONLY
```

Or secure the endpoint with Spring Security.

### Health Indicator

The `spring-actuator` module registers an `endpointGate` health component that is exposed under `/actuator/health`.

When the provider responds normally, the component reports `UP` with gate statistics:

```json
{
  "status": "UP",
  "components": {
    "endpointGate": {
      "status": "UP",
      "details": {
        "provider": "MutableInMemoryEndpointGateProvider",
        "totalGates": 2,
        "enabledGates": 1,
        "disabledGates": 1,
        "defaultEnabled": false
      }
    }
  }
}
```

If an exception occurs during the health check, the component reports `DOWN`.

### Event integration

An `EndpointGateChangedEvent` is published every time a gate is updated via the actuator endpoint. An `EndpointGateRemovedEvent` is published when a gate that existed is deleted. An `EndpointGateScheduleChangedEvent` is published when a gate's schedule is set, updated, or removed. Subscribe with `@EventListener` to react to changes (e.g., clearing caches, logging audit trails).


> **WebFlux (reactive) environments:** Events are published synchronously on the calling thread, which may be the Netty event loop thread. Listeners must not perform blocking operations directly; use `@Async` or subscribe on `Schedulers.boundedElastic()` to offload blocking work.

```java
@Component
class EndpointGateChangeListener {

  @EventListener
  void onGateChanged(EndpointGateChangedEvent event) {
    log.info("Gate '{}' changed to {}", event.gateId(), event.enabled());
  }

  @EventListener
  void onGateRemoved(EndpointGateRemovedEvent event) {
    log.info("Gate '{}' was removed", event.gateId());
  }

  @EventListener
  void onGateScheduleChanged(EndpointGateScheduleChangedEvent event) {
    if (event.schedule() != null) {
      log.info("Gate '{}' schedule updated to {}", event.gateId(), event.schedule());
    } else {
      log.info("Gate '{}' schedule removed", event.gateId());
    }
  }
}
```

## Micrometer Metrics

The `spring-metrics` module records Micrometer metrics for every gate evaluation. Add the dependency and ensure a `MeterRegistry` bean is present (e.g., via `spring-boot-starter-actuator`).

### Recorded Metrics

| Metric | Type | Tags | Description |
|---|---|---|---|
| `endpoint.gate.evaluations` | Counter | `gate.id`, `outcome` | Total number of gate evaluations |
| `endpoint.gate.evaluation.duration` | Timer | `gate.id`, `outcome` | Duration of gate evaluations |

### Outcome Tag Values

| Value | Description |
|---|---|
| `allowed` | Access was allowed |
| `denied.disabled` | Gate is disabled |
| `denied.schedule_inactive` | Gate schedule is not active |
| `denied.condition_not_met` | Condition evaluated to false |
| `denied.rollout_excluded` | Request is outside the rollout bucket |

The module auto-configures when a `MeterRegistry` bean is present. No additional configuration is required.

## Contributing

Please see [the contribution guide](.github/CONTRIBUTING.md) and the [Code of conduct](.github/CODE_OF_CONDUCT.md) before contributing.
