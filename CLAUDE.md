# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**endpoint-gate** is a multi-module Gradle project (Java 25, Spring Boot 4.x) that provides endpoint gate support for Spring MVC and Spring WebFlux applications. Published to Maven Central under group `net.bright-room.endpoint-gate`.

## Development Commands

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

For modules, dependency graph, request flow, extension points, and configuration reference, see @.claude/rules/architecture.md

## Coding Guidelines

For coding conventions (CG-1 through CG-9), see @.claude/rules/coding.md

## Contributing

PRs target `main`. PR titles should be prefixed with `Close #<IssueNumber>` when resolving an issue. See @.github/CONTRIBUTING.md for the full workflow.
