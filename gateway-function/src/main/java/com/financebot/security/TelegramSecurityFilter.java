package com.financebot.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class TelegramSecurityFilter {
    private static final String SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    @ConfigProperty(name = "telegram.secret-token")
    String secretToken;

    @ServerRequestFilter
    public Response filter(ContainerRequestContext ctx) {
        if (!ctx.getUriInfo().getPath().startsWith("/webhook"))
            return null;

        String token = ctx.getHeaderString(SECRET_HEADER);
        if (!telegramTokenIsValid(token))
            return Response.status(Response.Status.UNAUTHORIZED).build();

        return null;
    }

    private boolean telegramTokenIsValid(String token) {
        return MessageDigest.isEqual(
                secretToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8)
        );
    }
}
