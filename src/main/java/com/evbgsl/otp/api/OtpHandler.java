package com.evbgsl.otp.api;

import com.evbgsl.otp.dto.ApiResponse;
import com.evbgsl.otp.dto.OtpGenerateRequest;
import com.evbgsl.otp.dto.OtpGenerateResponse;
import com.evbgsl.otp.dto.OtpValidateRequest;
import com.evbgsl.otp.model.User;
import com.evbgsl.otp.service.OtpService;
import com.evbgsl.otp.service.TokenService;
import com.evbgsl.otp.util.AuthUtil;
import com.evbgsl.otp.util.JsonUtil;
import com.evbgsl.otp.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class OtpHandler implements HttpHandler {

    private final TokenService tokenService;
    private final OtpService otpService;

    public OtpHandler(TokenService tokenService, OtpService otpService) {
        this.tokenService = tokenService;
        this.otpService = otpService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String token = AuthUtil.extractBearerToken(exchange);
            User user = tokenService.requireAuth(token);

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("POST".equalsIgnoreCase(method) && "/api/otp/generate".equals(path)) {
                handleGenerate(exchange, user);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && "/api/otp/validate".equals(path)) {
                handleValidate(exchange, user);
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

    private void handleGenerate(HttpExchange exchange, User user) throws IOException {
        OtpGenerateRequest request = JsonUtil.fromJson(
                exchange.getRequestBody(),
                OtpGenerateRequest.class
        );

        OtpGenerateResponse response = otpService.generate(user, request);

        ResponseUtil.sendJson(exchange, 201, response);
    }

    private void handleValidate(HttpExchange exchange, User user) throws IOException {
        OtpValidateRequest request = JsonUtil.fromJson(
                exchange.getRequestBody(),
                OtpValidateRequest.class
        );

        otpService.validate(user, request);

        ResponseUtil.sendJson(exchange, 200, new ApiResponse("OTP code validated successfully"));
    }
}