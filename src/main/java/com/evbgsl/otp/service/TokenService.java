package com.evbgsl.otp.service;

import com.evbgsl.otp.model.AuthToken;
import com.evbgsl.otp.model.Role;
import com.evbgsl.otp.model.User;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenService {

    private static final long TOKEN_TTL_SECONDS = 3600;

    private final Map<String, AuthToken> tokens = new ConcurrentHashMap<>();

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(TOKEN_TTL_SECONDS);

        tokens.put(token, new AuthToken(token, user, expiresAt));

        return token;
    }

    public User getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        AuthToken authToken = tokens.get(token);

        if (authToken == null) {
            throw new IllegalArgumentException("Invalid token");
        }

        if (authToken.isExpired()) {
            tokens.remove(token);
            throw new IllegalArgumentException("Token expired");
        }

        return authToken.getUser();
    }

    public User requireAuth(String token) {
        return getUserByToken(token);
    }

    public User requireRole(String token, Role requiredRole) {
        User user = getUserByToken(token);

        if (user.getRole() != requiredRole) {
            throw new SecurityException("Access denied");
        }

        return user;
    }

    public long getTokenTtlSeconds() {
        return TOKEN_TTL_SECONDS;
    }
}