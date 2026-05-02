package com.evbgsl.otp.notification;

import com.evbgsl.otp.model.User;

public interface NotificationService {

    void sendCode(User user, String operationId, String code, String destination);
}