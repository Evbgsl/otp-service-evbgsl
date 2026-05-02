package com.evbgsl.otp.notification;

import com.evbgsl.otp.model.User;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationService implements NotificationService {

    private final String botToken;
    private final String telegramApiUrl;
    private final HttpClient httpClient;

    public TelegramNotificationService() {
        Properties config = loadConfig();

        this.botToken = config.getProperty("telegram.bot.token");
        this.telegramApiUrl = config.getProperty("telegram.api.url");
        this.httpClient = HttpClient.newHttpClient();

        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("telegram.bot.token is required");
        }

        if (telegramApiUrl == null || telegramApiUrl.isBlank()) {
            throw new IllegalStateException("telegram.api.url is required");
        }
    }

    @Override
    public void sendCode(User user, String operationId, String code, String destination) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Telegram chat id is required");
        }

        String text = """
                Hello, %s!

                Your OTP code for operation %s is: %s

                If you did not request this code, please ignore this message.
                """.formatted(user.getLogin(), operationId, code);

        String url = telegramApiUrl
                + botToken
                + "/sendMessage?chat_id="
                + urlEncode(destination)
                + "&text="
                + urlEncode(text);

        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("Telegram API error. Status code: "
                        + response.statusCode()
                        + ", body: "
                        + response.body());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Telegram sending interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP code by Telegram", e);
        }
    }

    private Properties loadConfig() {
        try (InputStream inputStream = TelegramNotificationService.class
                .getClassLoader()
                .getResourceAsStream("telegram.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException("telegram.properties not found");
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load telegram configuration", e);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}