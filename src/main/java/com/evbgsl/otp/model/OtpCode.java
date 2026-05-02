package com.evbgsl.otp.model;

import java.time.LocalDateTime;

public class OtpCode {

    private final Long id;
    private final Long userId;
    private final String operationId;
    private final String code;
    private final OtpStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final LocalDateTime usedAt;

    public OtpCode(
            Long id,
            Long userId,
            String operationId,
            String code,
            OtpStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime usedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.operationId = operationId;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getCode() {
        return code;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}