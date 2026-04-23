# GitHub Activity Stream

A Spring Boot application that streams public GitHub activity in near real time. It polls the GitHub public events API, publishes events to Kafka, consumes them back into the application, stores them in PostgreSQL, exposes query endpoints, and broadcasts live updates to a small browser dashboard through WebSockets.

This project is intentionally useful as a learning playground for event-driven backend development with Spring Boot, Kafka, WebSockets, Flyway, JPA, and PostgreSQL.

## What It Does

- Polls `https://api.github.com/events` every 5 seconds.
- Publishes each GitHub event to the Kafka topic `github-public-events`.
- Consumes Kafka messages and persists them to PostgreSQL.
- Broadcasts live event payloads to WebSocket subscribers on `/topic/github-events`.
- Exposes REST endpoints for querying stored events by page, repository, type, recency, and processing state.
- Serves a simple static dashboard from `src/main/resources/static/index.html`.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring WebSocket with STOMP and SockJS
- Spring for Apache Kafka
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- Maven
- Docker Compose

## Architecture

```text
GitHub Public Events API
          |
          | scheduled poll every 5 seconds
          v
GitHubPublicEventsService
          |
          | Kafka message
          v
github-public-events topic
          |
          | @KafkaListener
          v
GitHubEventConsumer
          |
          +--> PostgreSQL github_events table
          |
          +--> WebSocket /topic/github-events
```

## Prerequisites

- JDK 21
- Docker and Docker Compose
- Maven wrapper support through `./mvnw`

You do not need to install Kafka or PostgreSQL locally. They are provided by `docker-compose.yaml`.

## Running Locally

Start Kafka, ZooKeeper, and PostgreSQL:

```bash
docker compose up -d
```

Run the application:

```bash
./mvnw spring-boot:run
```

Open the dashboard:

```text
http://localhost:8080
```

The application expects:

- Kafka at `localhost:9092`
- PostgreSQL at `localhost:5433`
- Database name `github_events`
- Database user `postgres`
- Database password `password`

These defaults are defined in `src/main/resources/application.yaml` and `docker-compose.yaml`.

## API Endpoints

Base path:

```text
/api/v1/events
```

List events with pagination:

```http
GET /api/v1/events?page=0&size=10
```

Find events by repository name:

```http
GET /api/v1/events/repo?repoName=owner/repository
```

Find events by GitHub event type:

```http
GET /api/v1/events/type?type=PushEvent
```

Find recent events:

```http
GET /api/v1/events/recent?hours=1
```

Find unprocessed events:

```http
GET /api/v1/events/unprocessed
```

## WebSocket Endpoint

The WebSocket endpoint is:

```text
/ws
```

Clients subscribe to:

```text
/topic/github-events
```

The static dashboard uses SockJS and STOMP to receive live events in the browser.

## Database

Flyway creates the `github_events` table on startup.

Stored fields include:

- `id`
- `type`
- `repo_name`
- `actor_login`
- `created_at`
- `payload`
- `processed`

Indexes are created for common lookups by repository, event type, creation time, and processing state.

## Running Tests

```bash
./mvnw test
```

The test profile uses an in-memory H2 database, disables Flyway, disables the scheduled GitHub poller, and prevents Kafka listeners from auto-starting.

## Useful Development Commands

Start dependencies:

```bash
docker compose up -d
```

Stop dependencies:

```bash
docker compose down
```

Stop dependencies and remove the PostgreSQL volume:

```bash
docker compose down -v
```

Run the application:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

## Project Structure

```text
src/main/java/me/manulorenzo/github_activity_stream
├── config       # RestTemplate and WebSocket configuration
├── controller   # REST API endpoints
├── domain       # GitHub API event model
├── dto          # API response DTOs
├── entity       # JPA entities
├── event        # Kafka consumers
├── mapper       # MapStruct mappers
├── repository   # Spring Data repositories
└── service      # GitHub polling and event query logic

src/main/resources
├── application.yaml
├── db/V1__Create_github_database_events_table.sql
└── static/index.html
```

## Configuration Notes

The scheduled GitHub polling service is controlled by:

```yaml
github:
  public-events:
    enabled: true
```

It is enabled by default. Set it to `false` in a profile if you want to run the app without polling GitHub.

## Learning-Focused Future Improvements

- Add integration tests with Testcontainers for Kafka and PostgreSQL.
- Add unit tests for the service, mapper, controller, and Kafka consumer layers.
- Add validation for query parameters such as page size, event type, and recency range.
- Add global exception handling with meaningful API error responses.
- Replace `System.err.println` with structured logging.
- Add retry and backoff behavior for GitHub API failures and Kafka publishing failures.
- Handle GitHub API rate limits explicitly, including response headers and cooldown behavior.
- Deduplicate GitHub events by GitHub event ID to avoid storing repeated poll results.
- Store the GitHub event `created_at` value from the API instead of only the ingestion timestamp.
- Add a dead-letter topic for events that cannot be deserialized or persisted.
- Add metrics with Spring Boot Actuator and Micrometer.
- Add OpenAPI documentation for the REST API.
- Improve the dashboard with historical event loading, reconnect handling, and richer filters.
- Fix the dashboard's historical fetch path so it uses the current `/api/v1/events/recent` endpoint.
- Add authentication or admin-only controls before exposing operational actions.
- Add Dockerfile support so the whole application can run through Docker Compose.
- Add CI to run tests and verify the project builds on every pull request.
- Add database migration examples for schema changes beyond the initial table.
- Explore Kafka message schemas with Avro, JSON Schema, or Protobuf.
- Add event processing workflows that update the `processed` field.
- Add filtering by actor login and full-text search over payloads.

## Current Scope

This is a learning project, not a production-ready GitHub analytics system. The core pipeline is intentionally simple so each moving part is easy to inspect, run, and extend.
