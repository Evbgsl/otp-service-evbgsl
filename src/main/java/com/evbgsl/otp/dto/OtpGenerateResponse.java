package com.evbgsl.otp.dto;

public class OtpGenerateResponse {

    private final String operationId;
    private final String code;
    private final int ttlSeconds;

    public OtpGenerateResponse(String operationId, String code, int ttlSeconds) {
        this.operationId = operationId;
        this.code = code;
        this.ttlSeconds = ttlSeconds;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getCode() {
        return code;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }
}