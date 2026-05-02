package com.evbgsl.otp.service;

import com.evbgsl.otp.dao.OtpCodeDao;
import com.evbgsl.otp.dao.OtpConfigDao;
import com.evbgsl.otp.dto.OtpGenerateRequest;
import com.evbgsl.otp.dto.OtpGenerateResponse;
import com.evbgsl.otp.dto.OtpValidateRequest;
import com.evbgsl.otp.model.OtpCode;
import com.evbgsl.otp.model.OtpConfig;
import com.evbgsl.otp.model.OtpStatus;
import com.evbgsl.otp.model.User;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class OtpService {

    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpCodeDao otpCodeDao, OtpConfigDao otpConfigDao) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
    }

    public OtpGenerateResponse generate(User user, OtpGenerateRequest request) {
        validateGenerateRequest(request);

        OtpConfig config = otpConfigDao.getConfig();

        String code = generateNumericCode(config.getCodeLength());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.getTtlSeconds());

        otpCodeDao.create(
                user.getId(),
                request.getOperationId(),
                code,
                OtpStatus.ACTIVE,
                expiresAt
        );

        return new OtpGenerateResponse(
                request.getOperationId(),
                code,
                config.getTtlSeconds()
        );
    }

    public void validate(User user, OtpValidateRequest request) {
        validateOtpRequest(request);

        OtpCode otpCode = otpCodeDao.findActiveByUserIdAndOperationId(
                user.getId(),
                request.getOperationId()
        );

        if (otpCode == null) {
            throw new IllegalArgumentException("Active OTP code not found");
        }

        if (otpCode.isExpired()) {
            otpCodeDao.markExpired(otpCode.getId());
            throw new IllegalArgumentException("OTP code expired");
        }

        if (!otpCode.getCode().equals(request.getCode())) {
            throw new IllegalArgumentException("Invalid OTP code");
        }

        otpCodeDao.markUsed(otpCode.getId());
    }

    private void validateGenerateRequest(OtpGenerateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (request.getOperationId() == null || request.getOperationId().isBlank()) {
            throw new IllegalArgumentException("Operation id is required");
        }
    }

    private void validateOtpRequest(OtpValidateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (request.getOperationId() == null || request.getOperationId().isBlank()) {
            throw new IllegalArgumentException("Operation id is required");
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("OTP code is required");
        }
    }

    private String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }
}