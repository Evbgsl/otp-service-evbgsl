package com.evbgsl.otp.dao;

import com.evbgsl.otp.config.DatabaseConfig;
import com.evbgsl.otp.model.OtpConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OtpConfigDao {

    public OtpConfig getConfig() {
        String sql = """
                SELECT id, code_length, ttl_seconds
                FROM otp_config
                WHERE id = 1
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return new OtpConfig(
                        resultSet.getLong("id"),
                        resultSet.getInt("code_length"),
                        resultSet.getInt("ttl_seconds")
                );
            }

            throw new IllegalStateException("OTP config not found");

        } catch (Exception e) {
            throw new RuntimeException("Failed to get OTP config", e);
        }
    }

    public void updateConfig(int codeLength, int ttlSeconds) {
        String sql = """
                UPDATE otp_config
                SET code_length = ?, ttl_seconds = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = 1
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, codeLength);
            statement.setInt(2, ttlSeconds);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update OTP config", e);
        }
    }
}