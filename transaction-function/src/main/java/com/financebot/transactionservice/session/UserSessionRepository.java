package com.financebot.transactionservice.session;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserSessionRepository {
    private static final String PREFIX = "session:";
    private final ValueCommands<String, UserSession> sessions;

    public UserSessionRepository(RedisDataSource redis) {
        sessions = redis.value(UserSession.class);
    }

    public void save(Long userId, UserSession session) {
        sessions.set(PREFIX + userId, session, new SetArgs().ex(600));
    }

    public UserSession get(Long userId) {
        return sessions.get(PREFIX + userId);
    }

    public void remove(Long userId) {
        sessions.getdel(PREFIX + userId);
    }
}
