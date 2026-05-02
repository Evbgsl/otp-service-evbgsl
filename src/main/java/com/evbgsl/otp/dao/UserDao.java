package com.evbgsl.otp.dao;

import java.util.ArrayList;
import java.util.List;

import com.evbgsl.otp.config.DatabaseConfig;
import com.evbgsl.otp.model.Role;
import com.evbgsl.otp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    public void create(String login, String passwordHash, Role role) {
        String sql = """
                INSERT INTO users (login, password_hash, role)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2, passwordHash);
            statement.setString(3, role.name());

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public User findByLogin(String login) {
        String sql = """
                SELECT id, login, password_hash, role
                FROM users
                WHERE login = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by login", e);
        }
    }

    public boolean adminExists() {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE role = 'ADMIN'
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getInt(1) > 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to check admin existence", e);
        }
    }

    private User mapUser(ResultSet resultSet) throws Exception {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password_hash"),
                Role.valueOf(resultSet.getString("role"))
        );
    }

    public List<User> findAllUsers() {
        String sql = """
            SELECT id, login, password_hash, role
            FROM users
            WHERE role = 'USER'
            ORDER BY id
            """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }

            return users;

        } catch (Exception e) {
            throw new RuntimeException("Failed to find users", e);
        }
    }

    public boolean deleteById(Long id) {
        String sql = """
            DELETE FROM users
            WHERE id = ? AND role = 'USER'
            """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }


}