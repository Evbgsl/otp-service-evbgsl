package com.evbgsl.otp.notification;

import com.evbgsl.otp.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);

    private static final Path FILE_PATH = Path.of("otp-codes.txt");

    @Override
    public void sendCode(User user, String operationId, String code, String destination) {
        String line = String.format(
                "[%s] userId=%d, login=%s, operationId=%s, code=%s%n",
                LocalDateTime.now(),
                user.getId(),
                user.getLogin(),
                operationId,
                code
        );

        try {
            Files.writeString(
                    FILE_PATH,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            logger.info("OTP code written to file: userId={}, operationId={}",
                    user.getId(),
                    operationId);

        } catch (IOException e) {
            logger.error("Failed to write OTP code to file: userId={}, operationId={}",
                    user.getId(),
                    operationId,
                    e);
            throw new RuntimeException("Failed to write OTP code to file", e);
        }
    }
}