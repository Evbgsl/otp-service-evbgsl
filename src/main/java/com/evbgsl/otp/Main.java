package com.evbgsl.otp;

import com.evbgsl.otp.api.AdminHandler;
import com.evbgsl.otp.api.AuthHandler;
import com.evbgsl.otp.api.HealthHandler;
import com.evbgsl.otp.api.HttpServerProvider;
import com.evbgsl.otp.api.OtpHandler;
import com.evbgsl.otp.api.UserHandler;
import com.evbgsl.otp.dao.OtpCodeDao;
import com.evbgsl.otp.dao.OtpConfigDao;
import com.evbgsl.otp.dao.UserDao;
import com.evbgsl.otp.model.DeliveryChannel;
import com.evbgsl.otp.model.OtpConfig;
import com.evbgsl.otp.notification.EmailNotificationService;
import com.evbgsl.otp.notification.FileNotificationService;
import com.evbgsl.otp.notification.NotificationService;
import com.evbgsl.otp.notification.SmsNotificationService;
import com.evbgsl.otp.notification.TelegramNotificationService;
import com.evbgsl.otp.service.AdminService;
import com.evbgsl.otp.service.AuthService;
import com.evbgsl.otp.service.ExpirationService;
import com.evbgsl.otp.service.OtpService;
import com.evbgsl.otp.service.TokenService;
import com.evbgsl.otp.util.SchemaInitializer;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        // --- Инициализация БД ---
        SchemaInitializer.init();
        logger.info("Database schema initialized");

        // --- DAO ---
        UserDao userDao = new UserDao();
        OtpConfigDao otpConfigDao = new OtpConfigDao();
        OtpCodeDao otpCodeDao = new OtpCodeDao();

        // --- Конфигурация OTP ---
        OtpConfig config = otpConfigDao.getConfig();
        logger.info("OTP config loaded: length={}, ttlSeconds={}",
                config.getCodeLength(),
                config.getTtlSeconds());

        // --- Сервисы ---
        TokenService tokenService = new TokenService();
        AuthService authService = new AuthService(userDao, tokenService);
        AdminService adminService = new AdminService(userDao, otpConfigDao);

        // --- Notification services ---
        Map<DeliveryChannel, NotificationService> notificationServices = Map.of(
                DeliveryChannel.FILE, new FileNotificationService(),
                DeliveryChannel.EMAIL, new EmailNotificationService(),
                DeliveryChannel.TELEGRAM, new TelegramNotificationService(),
                DeliveryChannel.SMS, new SmsNotificationService()
        );

        OtpService otpService = new OtpService(
                otpCodeDao,
                otpConfigDao,
                notificationServices
        );

        // --- Expiration worker ---
        ExpirationService expirationService = new ExpirationService(otpCodeDao);
        expirationService.start();
        logger.info("OTP expiration service started");

        // --- HTTP Server ---
        int port = 8080;
        HttpServer server = HttpServerProvider.create(port);

        // --- Handlers ---
        server.createContext("/health", new HealthHandler());

        server.createContext("/api/auth/register", new AuthHandler(authService));
        server.createContext("/api/auth/login", new AuthHandler(authService));

        server.createContext("/api/user/profile", new UserHandler(tokenService));

        server.createContext("/api/admin/users", new AdminHandler(tokenService, adminService));
        server.createContext("/api/admin/otp-config", new AdminHandler(tokenService, adminService));

        server.createContext("/api/otp/generate", new OtpHandler(tokenService, otpService));
        server.createContext("/api/otp/validate", new OtpHandler(tokenService, otpService));

        // --- Start server ---
        server.start();
        logger.info("OTP Service started on port {}", port);
    }
}