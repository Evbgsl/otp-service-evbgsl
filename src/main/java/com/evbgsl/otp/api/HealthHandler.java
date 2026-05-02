package com.evbgsl.otp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(HealthHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Incoming request: method={}, path={}",
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath());

        String response = "OK";

        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        }

        logger.info("Health check completed: status=200");
    }
}