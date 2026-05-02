package com.evbgsl.otp.dao;

import com.evbgsl.otp.config.DatabaseConfig;
import com.evbgsl.otp.model.OtpCode;
import com.evbgsl.otp.model.OtpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class OtpCodeDao {

    public void create(Long userId, String operationId, String code, OtpStatus status, java.time.LocalDateTime expiresAt) {
        String sql = """
                INSERT INTO otp_codes (user_id, operation_id, code, status, expires_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, operationId);
            statement.setString(3, code);
            statement.setString(4, status.name());
            statement.setTimestamp(5, Timestamp.valueOf(expiresAt));

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create OTP code", e);
        }
    }

    public OtpCode findActiveByUserIdAndOperationId(Long userId, String operationId) {
        String sql = """
                SELECT id, user_id, operation_id, code, status, created_at, expires_at, used_at
                FROM otp_codes
                WHERE user_id = ?
                  AND operation_id = ?
                  AND status = 'ACTIVE'
                ORDER BY created_at DESC
                LIMIT 1
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, operationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOtpCode(resultSet);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to find active OTP code", e);
        }
    }

    public void markUsed(Long id) {
        String sql = """
                UPDATE otp_codes
                SET status = 'USED', used_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to mark OTP code as used", e);
        }
    }

    public void markExpired(Long id) {
        String sql = """
                UPDATE otp_codes
                SET status = 'EXPIRED'
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to mark OTP code as expired", e);
        }
    }

    private OtpCode mapOtpCode(ResultSet resultSet) throws Exception {
        Timestamp usedAt = resultSet.getTimestamp("used_at");

        return new OtpCode(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("operation_id"),
                resultSet.getString("code"),
                OtpStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("expires_at").toLocalDateTime(),
                usedAt == null ? null : usedAt.toLocalDateTime()
        );
    }

    public int markExpiredCodes() {
        String sql = """
            UPDATE otp_codes
            SET status = 'EXPIRED'
            WHERE status = 'ACTIVE'
              AND expires_at <= CURRENT_TIMESTAMP
            """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to mark expired OTP codes", e);
        }
    }
}