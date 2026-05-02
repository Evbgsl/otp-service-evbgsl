package com.evbgsl.otp.dto;

public class OtpGenerateRequest {

    private String operationId;
    private String deliveryChannel;

    public String getOperationId() {
        return operationId;
    }

    public String getDeliveryChannel() {
        return deliveryChannel;
    }
}