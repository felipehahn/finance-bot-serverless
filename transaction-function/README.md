# Transaction function

Java 21 / Quarkus Lambda migration of `finance-bot/transaction-service`. The package layout and command handlers are preserved. The gateway invokes this function asynchronously with a `TelegramCommandEvent`; replies go directly through the shared `TelegramClient`. No RabbitMQ is required.

Commands: `/start`, `/ajuda`, `/receita`, `/despesa`, `/relatorio`, `/deltransacao`, `/cancelar`, and the unknown-command fallback. Income and expense conversations retain category/date keyboards, amount and description validation, and relative dates. Reports retain current/custom months, income/expense filters, totals and balance. Deletion verifies transaction ownership. Redis sessions expire after ten minutes.

## Configuration

Set `DB_JDBC_URL` (for example `jdbc:postgresql://host:5432/financebot`), `DB_USERNAME`, `DB_PASSWORD`, `REDIS_URL`, and `TELEGRAM_BOT_TOKEN`. `TELEGRAM_API_URL` defaults to `https://api.telegram.org/bot`. Keep credentials outside source control.

The original Flyway migrations are preserved, so an existing database with Flyway history can be reused. Flyway applies pending migrations at startup; the database account needs migration permissions. Configure Lambda networking to reach PostgreSQL, Redis, and the Telegram API. Each execution environment uses at most two database connections.

## Build and test

From the repository root, using JDK 21:

```powershell
mvn -f finance-core/pom.xml install
mvn -f transaction-function/pom.xml test
mvn -f transaction-function/pom.xml package
```

Tests cover the Lambda command flows with mocked external services, JSON session round trips between invocations, ownership checks, user registration, and report month boundaries. They do not require running PostgreSQL, Redis, or a real bot.

For a Linux native Lambda artifact, with Docker running:

```powershell
mvn -f transaction-function/pom.xml package '-Dnative' '-DskipTests' '-Dquarkus.native.container-build=true' '-Dquarkus.native.march=x86-64-v2'
```

Upload `target/function.zip` to an x86_64 Lambda with runtime `provided.al2023` and set `DISABLE_SIGNAL_HANDLERS=true`. For the JVM ZIP use Java 21 and handler `io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest`.

Set the gateway's `processor.function-name` to this Lambda's name and allow its execution role to invoke it. The gateway already sends this event shape:

```json
{"chatId":123,"userId":456,"username":"Alice","command":"/start","text":null,"messageId":1}
```

For local development, supply the configuration above and run `mvn -f transaction-function/pom.xml quarkus:dev`. POST command events to the Lambda mock server on port 18082.
