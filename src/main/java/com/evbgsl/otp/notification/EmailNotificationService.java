package com.evbgsl.otp.notification;

import com.evbgsl.otp.model.User;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailNotificationService() {
        Properties config = loadConfig();

        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");

        this.session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public void sendCode(User user, String operationId, String code, String destination) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Email destination is required");
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("Your OTP Code");
            message.setText("""
                    Hello, %s!

                    Your OTP code for operation %s is: %s

                    If you did not request this code, please ignore this email.
                    """.formatted(user.getLogin(), operationId, code));

            Transport.send(message);

            logger.info("OTP email sent: userId={}, operationId={}, destination={}",
                    user.getId(),
                    operationId,
                    destination);

        } catch (MessagingException e) {
            logger.error("Failed to send OTP email: userId={}, operationId={}, destination={}",
                    user.getId(),
                    operationId,
                    destination,
                    e);
            throw new RuntimeException("Failed to send OTP code by email", e);
        }
    }

    private Properties loadConfig() {
        try (InputStream inputStream = EmailNotificationService.class
                .getClassLoader()
                .getResourceAsStream("email.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException("email.properties not found");
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }
}