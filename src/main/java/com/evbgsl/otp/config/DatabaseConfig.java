package com.evbgsl.otp.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        String url = PROPERTIES.getProperty("db.url");
        String username = PROPERTIES.getProperty("db.username");
        String password = PROPERTIES.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadProperties() {
        try (InputStream inputStream = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException("application.properties not found");
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }
}