package com.udmurt.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Простые сессии: токен (UUID) привязан к пользователю и живёт 24 часа. */
public final class Sessions {

    private static final long TTL_MS = 24 * 60 * 60 * 1000L;

    private static final class Session {
        final long userId;
        final long expiresAt;

        Session(long userId, long expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public String create(long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(userId, System.currentTimeMillis() + TTL_MS));
        return token;
    }

    /** Возвращает id пользователя по токену или -1, если токен недействителен. */
    public long resolve(String token) {
        if (token == null || token.isBlank()) return -1;
        Session session = sessions.get(token);
        if (session == null) return -1;
        if (System.currentTimeMillis() > session.expiresAt) {
            sessions.remove(token);
            return -1;
        }
        return session.userId;
    }
}