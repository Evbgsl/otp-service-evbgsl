package com.evbgsl.otp.api;

import com.evbgsl.otp.dto.ApiResponse;
import com.evbgsl.otp.dto.OtpConfigUpdateRequest;
import com.evbgsl.otp.model.Role;
import com.evbgsl.otp.service.AdminService;
import com.evbgsl.otp.service.TokenService;
import com.evbgsl.otp.util.AuthUtil;
import com.evbgsl.otp.util.JsonUtil;
import com.evbgsl.otp.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class AdminHandler implements HttpHandler {

    private final TokenService tokenService;
    private final AdminService adminService;

    public AdminHandler(TokenService tokenService, AdminService adminService) {
        this.tokenService = tokenService;
        this.adminService = adminService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String token = AuthUtil.extractBearerToken(exchange);
            tokenService.requireRole(token, Role.ADMIN);

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method) && "/api/admin/users".equals(path)) {
                handleGetUsers(exchange);
                return;
            }

            if ("PUT".equalsIgnoreCase(method) && "/api/admin/otp-config".equals(path)) {
                handleUpdateOtpConfig(exchange);
                return;
            }

            if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/api/admin/users/")) {
                handleDeleteUser(exchange, path);
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

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        ResponseUtil.sendJson(exchange, 200, adminService.getAllUsers());
    }

    private void handleUpdateOtpConfig(HttpExchange exchange) throws IOException {
        OtpConfigUpdateRequest request = JsonUtil.fromJson(
                exchange.getRequestBody(),
                OtpConfigUpdateRequest.class
        );

        adminService.updateOtpConfig(request);

        ResponseUtil.sendJson(exchange, 200, new ApiResponse("OTP config updated successfully"));
    }

    private void handleDeleteUser(HttpExchange exchange, String path) throws IOException {
        Long userId = extractUserId(path);

        adminService.deleteUser(userId);

        ResponseUtil.sendJson(exchange, 200, new ApiResponse("User deleted successfully"));
    }

    private Long extractUserId(String path) {
        String prefix = "/api/admin/users/";
        String rawId = path.substring(prefix.length());

        try {
            return Long.parseLong(rawId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid user id");
        }
    }
}