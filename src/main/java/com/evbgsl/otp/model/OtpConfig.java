package com.evbgsl.otp.model;

public class OtpConfig {

    private Long id;
    private int codeLength;
    private int ttlSeconds;

    public OtpConfig(Long id, int codeLength, int ttlSeconds) {
        this.id = id;
        this.codeLength = codeLength;
        this.ttlSeconds = ttlSeconds;
    }

    public Long getId() {
        return id;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }
}