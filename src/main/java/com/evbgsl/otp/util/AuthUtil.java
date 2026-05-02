package com.evbgsl.otp.util;

import com.sun.net.httpserver.HttpExchange;

public class AuthUtil {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private AuthUtil() {
    }

    public static String extractBearerToken(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER);

        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("Authorization header is required");
        }

        if (!header.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Authorization header must start with Bearer");
        }

        String token = header.substring(BEARER_PREFIX.length());

        if (token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        return token;
    }
}