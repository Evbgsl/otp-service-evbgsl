package com.evbgsl.otp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : hashBytes) {
                result.append(String.format("%02x", b));
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}