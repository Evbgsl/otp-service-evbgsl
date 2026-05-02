package com.evbgsl.otp.service;

import com.evbgsl.otp.dao.OtpCodeDao;
import com.evbgsl.otp.dao.OtpConfigDao;
import com.evbgsl.otp.dto.OtpGenerateRequest;
import com.evbgsl.otp.dto.OtpGenerateResponse;
import com.evbgsl.otp.dto.OtpValidateRequest;
import com.evbgsl.otp.model.DeliveryChannel;
import com.evbgsl.otp.model.OtpCode;
import com.evbgsl.otp.model.OtpConfig;
import com.evbgsl.otp.model.OtpStatus;
import com.evbgsl.otp.model.User;
import com.evbgsl.otp.notification.NotificationService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

public class OtpService {

    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final Map<DeliveryChannel, NotificationService> notificationServices;
    private final SecureRandom random = new SecureRandom();

    public OtpService(
            OtpCodeDao otpCodeDao,
            OtpConfigDao otpConfigDao,
            Map<DeliveryChannel, NotificationService> notificationServices
    ) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.notificationServices = notificationServices;
    }

    public OtpGenerateResponse generate(User user, OtpGenerateRequest request) {
        validateGenerateRequest(request);

        DeliveryChannel deliveryChannel = parseDeliveryChannel(request.getDeliveryChannel());

        validateDestination(deliveryChannel, request.getDestination());

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

        NotificationService notificationService = notificationServices.get(deliveryChannel);

        if (notificationService == null) {
            throw new IllegalArgumentException("Unsupported delivery channel: " + deliveryChannel);
        }

        notificationService.sendCode(
                user,
                request.getOperationId(),
                code,
                request.getDestination()
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

        if (request.getDeliveryChannel() == null || request.getDeliveryChannel().isBlank()) {
            throw new IllegalArgumentException("Delivery channel is required");
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

    private DeliveryChannel parseDeliveryChannel(String value) {
        try {
            return DeliveryChannel.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown delivery channel: " + value);
        }
    }

    private String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }

    private void validateDestination(DeliveryChannel deliveryChannel, String destination) {
        if (deliveryChannel == DeliveryChannel.EMAIL) {
            if (destination == null || destination.isBlank()) {
                throw new IllegalArgumentException("Email destination is required");
            }

            if (!destination.contains("@")) {
                throw new IllegalArgumentException("Invalid email destination");
            }
        }
    }
}