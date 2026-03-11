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

Multi-module Gradle project (Java 25, Spring Boot 4.x) for endpoint gate support in Spring MVC / WebFlux. Published to Maven Central under group `net.bright-room.endpoint-gate`.

For full architecture details (modules, dependency graph, request flow, extension points, configuration reference), see @.claude/rules/architecture.md

## Coding Guidelines

コード実装時は @.claude/rules/coding.md を参照し、ガイドラインに準拠したコードを書くこと。

## Contributing

PRs target `main`. PR titles should be prefixed with `Close #<IssueNumber>` when resolving an issue. See @.github/CONTRIBUTING.md for the full workflow.
