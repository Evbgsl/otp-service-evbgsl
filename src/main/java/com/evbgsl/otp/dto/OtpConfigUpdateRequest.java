package com.evbgsl.otp.dto;

public class OtpConfigUpdateRequest {

    private int codeLength;
    private int ttlSeconds;

    public int getCodeLength() {
        return codeLength;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }
}