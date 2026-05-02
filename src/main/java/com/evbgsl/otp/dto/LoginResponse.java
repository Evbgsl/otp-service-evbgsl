package com.evbgsl.otp.dto;

public class LoginResponse {

    private final String token;
    private final long expiresInSeconds;

    public LoginResponse(String token, long expiresInSeconds) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}