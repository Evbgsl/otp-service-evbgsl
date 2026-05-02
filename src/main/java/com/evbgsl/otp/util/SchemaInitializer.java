package com.evbgsl.otp.util;

import com.evbgsl.otp.config.DatabaseConfig;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void init() {
        try (InputStream inputStream = SchemaInitializer.class
                .getClassLoader()
                .getResourceAsStream("schema.sql")) {

            if (inputStream == null) {
                throw new IllegalStateException("schema.sql not found");
            }

            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            try (Connection connection = DatabaseConfig.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.execute(sql);
                System.out.println("Database schema initialized");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}