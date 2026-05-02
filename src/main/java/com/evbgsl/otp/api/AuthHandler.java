package com.evbgsl.otp.api;

import com.evbgsl.otp.dto.ApiResponse;
import com.evbgsl.otp.dto.LoginRequest;
import com.evbgsl.otp.dto.LoginResponse;
import com.evbgsl.otp.dto.RegisterRequest;
import com.evbgsl.otp.service.AuthService;
import com.evbgsl.otp.util.JsonUtil;
import com.evbgsl.otp.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class AuthHandler implements HttpHandler {

    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("POST".equalsIgnoreCase(method) && "/api/auth/register".equals(path)) {
                handleRegister(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
                handleLogin(exchange);
                return;
            }

            ResponseUtil.sendJson(exchange, 404, new ApiResponse("Not found"));

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendJson(exchange, 400, new ApiResponse(e.getMessage()));
        } catch (SecurityException e) {
            ResponseUtil.sendJson(exchange, 403, new ApiResponse(e.getMessage()));
        } catch (Exception e) {
            ResponseUtil.sendJson(exchange, 500, new ApiResponse("Internal server error"));
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        RegisterRequest request = JsonUtil.fromJson(exchange.getRequestBody(), RegisterRequest.class);

        authService.register(request);

        ResponseUtil.sendJson(exchange, 201, new ApiResponse("User registered successfully"));
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        LoginRequest request = JsonUtil.fromJson(exchange.getRequestBody(), LoginRequest.class);

        LoginResponse response = authService.login(request);

        ResponseUtil.sendJson(exchange, 200, response);
    }
}