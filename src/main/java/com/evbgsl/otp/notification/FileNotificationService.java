package com.evbgsl.otp.notification;

import com.evbgsl.otp.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class FileNotificationService implements NotificationService {

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
        } catch (IOException e) {
            throw new RuntimeException("Failed to write OTP code to file", e);
        }
    }
}