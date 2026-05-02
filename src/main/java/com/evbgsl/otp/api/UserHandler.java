package com.evbgsl.otp.api;

import com.evbgsl.otp.dto.ApiResponse;
import com.evbgsl.otp.model.User;
import com.evbgsl.otp.service.TokenService;
import com.evbgsl.otp.util.AuthUtil;
import com.evbgsl.otp.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);


    private final TokenService tokenService;

    public UserHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Incoming user request: method={}, path={}",
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath());

        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method) && "/api/user/profile".equals(path)) {
                handleProfile(exchange);
                return;
            }

            ResponseUtil.sendJson(exchange, 404, new ApiResponse("Not found"));

        } catch (IllegalArgumentException e) {
            logger.warn("User request unauthorized or invalid: {}", e.getMessage());
            ResponseUtil.sendJson(exchange, 401, new ApiResponse(e.getMessage()));
        } catch (SecurityException e) {
            logger.warn("User request forbidden: {}", e.getMessage());
            ResponseUtil.sendJson(exchange, 403, new ApiResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected user request error", e);
            ResponseUtil.sendJson(exchange, 500, new ApiResponse("Internal server error"));
        }
    }

    private void handleProfile(HttpExchange exchange) throws IOException {
        String token = AuthUtil.extractBearerToken(exchange);
        User user = tokenService.requireAuth(token);

        Map<String, Object> response = Map.of(
                "id", user.getId(),
                "login", user.getLogin(),
                "role", user.getRole().name()
        );

        logger.info("User profile requested: userId={}, login={}",
                user.getId(),
                user.getLogin());

        ResponseUtil.sendJson(exchange, 200, response);
    }
}