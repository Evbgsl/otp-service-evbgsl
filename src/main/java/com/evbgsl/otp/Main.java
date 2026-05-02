package com.evbgsl.otp;

import com.evbgsl.otp.api.AdminHandler;
import com.evbgsl.otp.api.AuthHandler;
import com.evbgsl.otp.api.HealthHandler;
import com.evbgsl.otp.api.HttpServerProvider;
import com.evbgsl.otp.api.UserHandler;
import com.evbgsl.otp.dao.OtpConfigDao;
import com.evbgsl.otp.dao.UserDao;
import com.evbgsl.otp.model.OtpConfig;
import com.evbgsl.otp.service.AdminService;
import com.evbgsl.otp.service.AuthService;
import com.evbgsl.otp.service.TokenService;
import com.evbgsl.otp.util.SchemaInitializer;
import com.sun.net.httpserver.HttpServer;

public class Main {

    public static void main(String[] args) throws Exception {
        SchemaInitializer.init();

        UserDao userDao = new UserDao();

        OtpConfigDao otpConfigDao = new OtpConfigDao();
        OtpConfig config = otpConfigDao.getConfig();

        System.out.println("OTP config: length=" + config.getCodeLength()
                + ", ttlSeconds=" + config.getTtlSeconds());

        TokenService tokenService = new TokenService();
        AuthService authService = new AuthService(userDao, tokenService);
        AdminService adminService = new AdminService(userDao, otpConfigDao);

        int port = 8080;

        HttpServer server = HttpServerProvider.create(port);

        server.createContext("/health", new HealthHandler());

        server.createContext("/api/auth/register", new AuthHandler(authService));
        server.createContext("/api/auth/login", new AuthHandler(authService));

        server.createContext("/api/user/profile", new UserHandler(tokenService));

        server.createContext("/api/admin/users", new AdminHandler(tokenService, adminService));
        server.createContext("/api/admin/otp-config", new AdminHandler(tokenService, adminService));

        server.start();

        System.out.println("OTP Service started on port " + port);
    }
}