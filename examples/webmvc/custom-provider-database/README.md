# custom-provider-database

## Overview

This module demonstrates how to implement a custom `EndpointGateProvider` that reads endpoint gate values from a database. It uses PostgreSQL as the data store and MyBatis as the persistence framework.

## What This Example Demonstrates

- **Custom EndpointGateProvider** -- Implementing `EndpointGateProvider` to load endpoint gate state from a relational database (`DatabaseEndpointGateProvider`).
- **MyBatis mapper** -- Using `GateManagementMapper` with an XML mapper definition to query the `gate_management` table.
- **Spring Boot Docker Compose support** -- Automatically starting a PostgreSQL container when the application boots, removing the need for manual database setup.
- **Database-driven gate evaluation** -- The `@EndpointGate` annotation on controller methods is evaluated against values stored in the database at runtime.

## How to Run

Docker must be running before starting the application. The PostgreSQL container is managed automatically via Spring Boot Docker Compose support.

```bash
./gradlew :examples:webmvc:custom-provider-database:bootRun
```

On first startup, the database is initialized with the schema and seed data defined in `docker/sql/init.sql`.

## Endpoints

| Endpoint | Endpoint Gate | Gate Value (init data) | Expected Behavior |
|---|---|---|---|
| `GET /api/experimental` | `experimental` | `true` | 200 -- returns response |

## Configuration

### application.yml

The configuration file sets up Docker Compose integration and the PostgreSQL datasource:

```yaml
spring:
  docker:
    compose:
      file: ../../docker/compose.yaml
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
```

### Database Schema

The `gate_management` table stores endpoint gate state:

| Column | Type | Constraint |
|---|---|---|
| `gate_id` | `varchar(100)` | Primary key |
| `enabled` | `boolean` | Not null |

### Initial Data

The following records are inserted on database initialization:

| gate_id | enabled |
|---|---|
| `experimental` | `true` |
| `development` | `false` |
