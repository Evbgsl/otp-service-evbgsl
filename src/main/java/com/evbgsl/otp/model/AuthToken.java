package com.evbgsl.otp.model;

import java.time.LocalDateTime;

public class AuthToken {

    private final String token;
    private final User user;
    private final LocalDateTime expiresAt;

    public AuthToken(String token, User user, LocalDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}