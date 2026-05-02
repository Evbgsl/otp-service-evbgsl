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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OtpHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(OtpHandler.class);

    private final TokenService tokenService;
    private final OtpService otpService;

    public OtpHandler(TokenService tokenService, OtpService otpService) {
        this.tokenService = tokenService;
        this.otpService = otpService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Incoming OTP request: method={}, path={}",
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath());

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
            logger.warn("OTP request failed: {}", e.getMessage());
            ResponseUtil.sendJson(exchange, 400, new ApiResponse(e.getMessage()));
        } catch (SecurityException e) {
            logger.warn("OTP request forbidden: {}", e.getMessage());
            ResponseUtil.sendJson(exchange, 403, new ApiResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected OTP request error", e);
            ResponseUtil.sendJson(exchange, 500, new ApiResponse("Internal server error"));
        }
    }

    private void handleGenerate(HttpExchange exchange, User user) throws IOException {
        OtpGenerateRequest request = JsonUtil.fromJson(
                exchange.getRequestBody(),
                OtpGenerateRequest.class
        );

        OtpGenerateResponse response = otpService.generate(user, request);

        logger.info("OTP generated: userId={}, login={}, operationId={}, deliveryChannel={}",
                user.getId(),
                user.getLogin(),
                request.getOperationId(),
                request.getDeliveryChannel());

        ResponseUtil.sendJson(exchange, 201, response);
    }

    private void handleValidate(HttpExchange exchange, User user) throws IOException {
        OtpValidateRequest request = JsonUtil.fromJson(
                exchange.getRequestBody(),
                OtpValidateRequest.class
        );

        otpService.validate(user, request);

        logger.info("OTP validated successfully: userId={}, login={}, operationId={}",
                user.getId(),
                user.getLogin(),
                request.getOperationId());

        ResponseUtil.sendJson(exchange, 200, new ApiResponse("OTP code validated successfully"));
    }


}