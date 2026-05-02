package com.evbgsl.otp;

import com.evbgsl.otp.api.HealthHandler;
import com.evbgsl.otp.api.HttpServerProvider;
import com.sun.net.httpserver.HttpServer;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8080;

        HttpServer server = HttpServerProvider.create(port);
        server.createContext("/health", new HealthHandler());
        server.start();

        System.out.println("OTP Service started on port " + port);
    }
}