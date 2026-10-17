package com.financebot.idempotency;

import io.quarkus.redis.datasource.RedisDataSource;
import io.vertx.mutiny.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;

@ApplicationScoped
public class IdempotencyService {

    private static final String CACHE_PREFIX = "idempotency:telegram:";
    private static final long TTL_SECONDS = Duration.ofHours(24).toSeconds();

    private final RedisDataSource redis;

    public IdempotencyService(RedisDataSource redis) {
        this.redis = redis;
    }

    public boolean isNew(String eventId) {
        Response response = redis.execute(
                "SET", CACHE_PREFIX + eventId, "1", "NX", "EX", String.valueOf(TTL_SECONDS));
        return response != null;
    }
}
