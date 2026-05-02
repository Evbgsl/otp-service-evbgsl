package com.evbgsl.otp.dto;

public class OtpGenerateRequest {

    private String operationId;
    private String deliveryChannel;
    private String destination;

    public String getOperationId() {
        return operationId;
    }

    public String getDeliveryChannel() {
        return deliveryChannel;
    }

    public String getDestination() {
        return destination;
    }
}