# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules
./gradlew build

# Run unit tests for a specific module
./gradlew :core:test
./gradlew :spring:webmvc:test

# Run integration tests for a specific module
./gradlew :spring:webmvc:integrationTest

# Run a single integration test class
./gradlew :spring:webmvc:integrationTest --tests "net.brightroom.endpointgate.spring.webmvc.EndpointGateInterceptorJsonResponseIntegrationTest"

# Run all checks (Spotless + unit tests + integration tests)
./gradlew check

# Apply Google Java Format
./gradlew spotlessApply
```

Code formatting uses Google Java Format via Spotless. Always run `spotlessApply` before committing, or the CI `check` task will fail.

## Architecture

This is a multi-module Gradle project (Java 25, Spring Boot 4.x) that provides endpoint gate support for Spring MVC and Spring WebFlux applications. It is published to Maven Central under group `net.bright-room.endpoint-gate`.

### Modules

- **`core`** — Pure Java / JDK only. Annotation (`@EndpointGate`), configuration POJOs, provider SPI (`EndpointGateProvider`/`MutableEndpointGateProvider`), evaluation pipeline (`EndpointGateEvaluationPipeline` with fixed order: Enabled → Schedule → Condition → Rollout), rollout strategy, and shared resolution logic. No Spring or Reactor dependency.
- **`reactive-core`** — Reactor dependency, depends on `core`. Reactive SPI (`ReactiveEndpointGateProvider`/`MutableReactiveEndpointGateProvider`), reactive evaluation pipeline (`ReactiveEndpointGateEvaluationPipeline`), reactive rollout strategy, and in-memory reactive provider implementations.
- **`spring/core` (artifactId: spring-core)** — Spring dependency, depends on `core`. `EndpointGateAutoConfiguration` bootstraps property binding (`EndpointGateProperties`, prefix `endpoint-gate`), SpEL condition evaluators, events (`EndpointGateChangedEvent`/`EndpointGateRemovedEvent`), and response builders (`ProblemDetailBuilder`/`HtmlResponseBuilder`).
- **`spring/webmvc` (artifactId: spring-webmvc)** — Spring MVC interceptor implementation. Depends on `spring-core`. Registers `InMemoryEndpointGateProvider` bean via `EndpointGateMvcAutoConfiguration`. Provides `EndpointGateInterceptor`, `EndpointGateHandlerFilterFunction` for functional endpoints, and `EndpointGateExceptionHandler`.
- **`spring/webflux` (artifactId: spring-webflux)** — Spring WebFlux AOP + HandlerFilterFunction implementation. Depends on `spring-core` + `reactive-core`. Uses `ReactiveEndpointGateProvider` and `EndpointGateAspect` for annotation-based controllers, `EndpointGateHandlerFilterFunction` for functional endpoints.
- **`spring/actuator` (artifactId: spring-actuator)** — Runtime gate management via Spring Boot Actuator endpoint (`/actuator/endpoint-gates`). Depends on `spring-core` + `reactive-core`. Auto-configuration is split into `ServletConfiguration` and `ReactiveConfiguration` via `@ConditionalOnWebApplication`. Publishes `EndpointGateChangedEvent`/`EndpointGateRemovedEvent` on gate changes. Auto-configured before webmvc/webflux. Health indicator reports `totalGates`/`enabledGates`/`disabledGates`.
- **`gradle-scripts`** — Composite build providing convention plugins: `java-conventions`, `spring-boot-starter`, `publish-plugin`, `spotless-java`, `spotless-kotlin`, `integration-test`.

### Module Dependency Graph

```
core
├── reactive-core
│   ├── spring-webflux
│   └── spring-actuator (ReactiveConfiguration)
└── spring-core
    ├── spring-webmvc
    ├── spring-webflux
    └── spring-actuator
```

### Package Structure

- `net.brightroom.endpointgate.core` — core module
- `net.brightroom.endpointgate.reactive.core` — reactive-core module
- `net.brightroom.endpointgate.spring.core` — spring-core module
- `net.brightroom.endpointgate.spring.webmvc` — spring-webmvc module
- `net.brightroom.endpointgate.spring.webflux` — spring-webflux module
- `net.brightroom.endpointgate.spring.actuator` — spring-actuator module

### Request Flow

1. `EndpointGateMvcInterceptorRegistrationAutoConfiguration` registers `EndpointGateInterceptor` for all paths (`/**`).
2. `EndpointGateInterceptor.preHandle()` checks `@EndpointGate` on the method first, then on the class. Method-level annotation takes priority.
3. `EndpointGateEvaluationPipeline` evaluates in fixed order: Enabled → Schedule → Condition → Rollout.
4. If the gate is disabled, `EndpointGateAccessDeniedException` is thrown.
5. If a schedule is configured for the gate (via `ScheduleProvider`) and it is currently inactive, `EndpointGateAccessDeniedException` is thrown.
6. If `condition` is non-empty, the SpEL expression is evaluated against request context variables (`headers`, `params`, `cookies`, `path`, `method`, `remoteAddress`). If the condition is not satisfied, `EndpointGateAccessDeniedException` is thrown.
7. If `rollout < 100`, the rollout percentage check is performed.
8. `EndpointGateExceptionHandler` (`@ControllerAdvice`, `@Order(Ordered.LOWEST_PRECEDENCE)`) catches the exception and delegates to `AccessDeniedInterceptResolution.resolve()` to write the response.

### Extension Points

- **Custom gate source**: Implement `EndpointGateProvider` (webmvc) or `ReactiveEndpointGateProvider` (webflux) and register as a `@Bean`. The default `InMemoryEndpointGateProvider` / `InMemoryReactiveEndpointGateProvider` reads from `endpoint-gate.gates` in config and is **fail-closed by default**. Set `endpoint-gate.default-enabled: true` to switch to fail-open. A custom bean replaces the default due to `@ConditionalOnMissingBean`.
- **Custom denied response**: Define a `@ControllerAdvice` that handles `EndpointGateAccessDeniedException`. It takes priority over the library's default handler.
- **Conditional access**: Configure `endpoint-gate.gates.<name>.condition` with a SpEL expression. Implement `EndpointGateConditionEvaluator` (core) or `ReactiveEndpointGateConditionEvaluator` (reactive-core) to replace the default SpEL evaluator. The `ConditionVariablesBuilder` in core centralizes the available variable key names (`headers`, `params`, `cookies`, `path`, `method`, `remoteAddress`). Configure fail-on-error behavior with `endpoint-gate.condition.fail-on-error`.
- **Gradual rollout**: Configure `endpoint-gate.gates.<name>.rollout` (0-100). Implement `EndpointGateContextResolver` (webmvc) or `ReactiveEndpointGateContextResolver` (webflux) for sticky rollout. Implement `RolloutStrategy` (core) or `ReactiveRolloutStrategy` (reactive-core) to customize bucketing.
- **Schedule-based activation**: Configure `endpoint-gate.gates.<name>.schedule.start/end/timezone` in properties to activate a gate only during a time window. Implement `ScheduleProvider` (core) or `ReactiveScheduleProvider` (reactive-core) and register as a `@Bean` to load schedules from a custom source. The `Schedule` record (`core/provider/`) is the SPI value type.

### Auto-configuration Registration

All modules use Spring Boot's `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Auto-configuration ordering:
1. `EndpointGateAutoConfiguration` (spring-core)
2. `EndpointGateActuatorAutoConfiguration` (spring-actuator) — before webmvc/webflux
3. `EndpointGateMvcAutoConfiguration` (spring-webmvc)
4. `EndpointGateMvcInterceptorRegistrationAutoConfiguration` (spring-webmvc)
5. `EndpointGateWebFluxAutoConfiguration` (spring-webflux)

### Response Types

Built-in `AccessDeniedInterceptResolution` implementations (selected by `endpoint-gate.response.type`):
- `PLAIN_TEXT` → `AccessDeniedInterceptResolutionViaPlainTextResponse`
- `HTML` → `AccessDeniedInterceptResolutionViaHtmlResponse`
- `JSON` (default) → `AccessDeniedInterceptResolutionViaJsonResponse` (RFC 7807 / Problem Details format)

### Configuration Reference

| Property | Default | Description |
|---|---|---|
| `endpoint-gate.gates.<id>.enabled` | `true` | Whether the gate is enabled |
| `endpoint-gate.gates.<id>.rollout` | — | Rollout percentage (0-100). If omitted, defaults to `100` (fully enabled) |
| `endpoint-gate.gates.<id>.condition` | `""` | SpEL condition expression |
| `endpoint-gate.gates.<id>.schedule.start` | — | Schedule start time (ISO 8601) |
| `endpoint-gate.gates.<id>.schedule.end` | — | Schedule end time (ISO 8601) |
| `endpoint-gate.gates.<id>.schedule.timezone` | — | Schedule timezone |
| `endpoint-gate.default-enabled` | `false` | Undefined gate policy (fail-closed / fail-open) |
| `endpoint-gate.response.type` | `JSON` | Response format: `JSON`, `PLAIN_TEXT`, `HTML` |
| `endpoint-gate.condition.fail-on-error` | `true` | SpEL error handling (fail-closed / fail-open) |
| `endpoint-gate.schedule.default-timezone` | — | Default timezone for schedule evaluation (fallback: gate-specific → this default → system default) |

## Coding Guidelines

コード実装時は `.claude/guidelines/coding.md` を参照し、ガイドラインに準拠したコードを書くこと。

## Contributing

PRs target `main`. PR titles should be prefixed with `Close #<IssueNumber>` when resolving an issue. See `.github/CONTRIBUTING.md` for the full workflow.
