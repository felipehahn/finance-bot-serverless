# Repository Guidelines

## Project Structure & Module Organization

This Java 21 Maven project is migrating to AWS Lambda. `finance-core/` holds shared Telegram DTOs, events, and client code. `gateway-function/` holds the Quarkus HTTP Lambda: webhook, security filter, Redis idempotency, and Lambda invoker. Java sources are in `src/main/java/com/financebot/`; tests are in `gateway-function/src/test/java/`. Module resources hold configuration. `docker-compose.yml` starts Redis. The root `pom.xml` lists an absent `transaction-function`; use module-level commands until the aggregator is updated.

## Build, Test, and Development Commands

Run these from the repository root with JDK 21 and Maven installed:

- `mvn -f finance-core/pom.xml install`: install the shared library locally for the gateway dependency.
- `mvn -f gateway-function/pom.xml test`: compile the gateway and run unit tests.
- `mvn -f gateway-function/pom.xml package`: build the gateway Lambda artifact.
- `mvn -f gateway-function/pom.xml quarkus:dev`: run the webhook locally with Quarkus live reload.
- `docker compose up -d redis`: start the Redis instance expected at `localhost:6379`.

On Windows, run `mvnw.cmd` from `gateway-function/` instead of `mvn`. Configure the processor function and Telegram settings for local runs; dummy test values are in `gateway-function/src/test/resources/application.properties`.

## Coding Style & Naming Conventions

Use four spaces for Java indentation and the existing `com.financebot` packages. Use `UpperCamelCase` for classes and `lowerCamelCase` for methods and fields. Keep shared request and event types in `finance-core`. No formatter or lint plugin is configured; match surrounding style.

## Testing Guidelines

Write test cases before implementing features or bug fixes; confirm they fail for the expected reason first. Consider existing regression tests and run the applicable suite after changes. For small, temporary adjustments made solely to enable a specific test, such as commenting out a downstream Lambda call for a gateway deployment test, new test cases are optional. Still verify the adjustment, for example by compiling the module. Gateway tests use JUnit 5, `@QuarkusTest`, and REST Assured. Name application tests `*Test` and packaged integration tests `*IT`. Native integration tests use the Maven `native` profile.

## Commit & Pull Request Guidelines

The short Git history uses brief, lowercase subjects (`init`, `migration gateway`); keep commits concise and specific. Pull requests should explain changed behavior, list test commands run, and link relevant issues. Include request or response examples for webhook changes.

## Security & Configuration

Keep bot tokens, Telegram secret tokens, and environment files out of commits. `.env` and `.env_prd` are ignored. Use environment variables or local configuration for real credentials; keep only dummy values in test resources.
