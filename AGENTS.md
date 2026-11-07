# Repository Guidelines

## Project Structure & Module Organization

This Java 21 Maven project is migrating to AWS Lambda. `finance-core/` holds shared Telegram DTOs, events, and client code. `gateway-function/` holds the Quarkus HTTP Lambda: webhook, security filter, Redis idempotency, and Lambda invoker. The gateway invokes `transaction-function/` asynchronously with a `TelegramCommandEvent`. Its `TransactionFunction` delegates to `service/FunctionService`, which dispatches through `command/CommandHandlerFactory` to `command/handler/` classes. `service/TelegramMessageService` sends replies directly through the shared Telegram client. PostgreSQL stores users and transactions; Redis stores conversation sessions. No message broker is required.

Java sources are in each module's `src/main/java/com/financebot/`; gateway and transaction tests are in their respective `src/test/java/` directories. Module resources hold configuration, with transaction Flyway migrations in `transaction-function/src/main/resources/db/migration/`. The root `pom.xml` lists all three modules; use the module-level commands below for focused builds. `docker-compose.yml` starts Redis only; provide PostgreSQL separately for transaction runtime and local development.

## Build, Test, and Development Commands

Run these from the repository root with JDK 21 and Maven installed:

- `mvn -f finance-core/pom.xml install`: install the shared library locally before building either function; rerun after shared code changes.
- `mvn -f gateway-function/pom.xml test`: compile the gateway and run unit tests.
- `mvn -f gateway-function/pom.xml package`: build the gateway Lambda artifact.
- `mvn -f gateway-function/pom.xml quarkus:dev`: run the webhook locally with Quarkus live reload.
- `mvn -f transaction-function/pom.xml test`: compile the transaction function and run its unit and command-flow tests.
- `mvn -f transaction-function/pom.xml package`: build the transaction JVM Lambda artifact at `transaction-function/target/function.zip`.
- `mvn -f transaction-function/pom.xml quarkus:dev`: run the transaction function locally; POST `TelegramCommandEvent` JSON to its Lambda mock server on port `18082`.
- `docker compose up -d redis`: start the Redis instance expected at `localhost:6379`.

On Windows, `gateway-function/mvnw.cmd` can replace installed Maven for gateway commands. Configure the processor function and Telegram settings for local runs; dummy test values are in `gateway-function/src/test/resources/application.properties`.

For transaction local development, configure `DB_JDBC_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `TELEGRAM_BOT_TOKEN`. `REDIS_URL` defaults to `redis://localhost:6379`; `TELEGRAM_API_URL` defaults to `https://api.telegram.org/bot`. Dev Services are disabled, so PostgreSQL and Redis must already be available. Flyway applies pending migrations at startup and Hibernate validates the schema. Transaction unit tests mock external services and require no running database, Redis, or Telegram credentials.

### Production Lambda Build (PowerShell)

Run `./build-lambdas.ps1` from the repository root to install the shared module, clean and test both functions, and build both native Lambda ZIPs. The script requires JDK 21, Maven, and a running Docker daemon, stops on command failure, and prints the artifact paths. It does not upload or deploy to AWS. Run `powershell -NoProfile -ExecutionPolicy Bypass -File tests/build-lambdas.Tests.ps1` to check the script with mocked build tools; these checks do not run native builds.

From the repository root, use JDK 21, Maven, and a running Docker daemon. Install the shared module once, run both function test suites, then build each Linux native executable:

```powershell
mvn -f finance-core/pom.xml install '-DskipTests'
mvn -f gateway-function/pom.xml test
mvn -f transaction-function/pom.xml test
mvn -f gateway-function/pom.xml package '-Dnative' '-DskipTests' '-Dquarkus.native.container-build=true' '-Dquarkus.native.march=x86-64-v2'
mvn -f transaction-function/pom.xml package '-Dnative' '-DskipTests' '-Dquarkus.native.container-build=true' '-Dquarkus.native.march=x86-64-v2'
```

Upload `gateway-function/target/function.zip` and `transaction-function/target/function.zip` to their respective x86_64 Lambdas using the `provided.al2023` runtime. Each ZIP contains its function's `bootstrap` executable; set `DISABLE_SIGNAL_HANDLERS=true` in both Lambda environments. Keep the PowerShell `-D` arguments quoted so Maven receives them intact. For a focused deployment, test and build only the affected function after installing the shared module.

Configure the transaction Lambda with the database, Redis, and Telegram variables listed above, and networking that reaches those services. Its database user needs Flyway migration permissions. Set the gateway's `processor.function-name` (`PROCESSOR_FUNCTION_NAME`) to the deployed transaction Lambda name and allow the gateway execution role to invoke it. For a transaction JVM deployment instead, run the regular `package` command and use the Java 21 runtime with handler `io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest`.

## Coding Style & Naming Conventions

Use four spaces for Java indentation and the existing `com.financebot` packages. Use `UpperCamelCase` for classes and `lowerCamelCase` for methods and fields. Keep shared request and event types in `finance-core`. No formatter or lint plugin is configured; match surrounding style.

## Testing Guidelines

Write test cases before implementing features or bug fixes; confirm they fail for the expected reason first. Consider existing regression tests and run the applicable suite after changes. For small, temporary adjustments made solely to enable a specific test, such as commenting out a downstream Lambda call for a gateway deployment test, new test cases are optional. Still verify the adjustment, for example by compiling the module. Gateway tests use JUnit 5, `@QuarkusTest`, and REST Assured. Transaction tests use JUnit 5 and Mockito, covering Lambda command flows, JSON session round trips, and service validation. Run both function suites when changing shared contracts or gateway-to-transaction invocation. Name application tests `*Test` and packaged integration tests `*IT`. Native integration tests use the Maven `native` profile.

## Commit & Pull Request Guidelines

The short Git history uses brief, lowercase subjects (`init`, `migration gateway`); keep commits concise and specific. Pull requests should explain changed behavior, list test commands run, and link relevant issues. Include request or response examples for webhook changes.

## Security & Configuration

Keep bot tokens, Telegram secret tokens, and environment files out of commits. `.env` and `.env_prd` are ignored. Use environment variables or local configuration for real credentials; keep only dummy values in test resources.
