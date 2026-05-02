package com.evbgsl.otp.service;

import com.evbgsl.otp.dao.UserDao;
import com.evbgsl.otp.dto.RegisterRequest;
import com.evbgsl.otp.model.Role;
import com.evbgsl.otp.model.User;
import com.evbgsl.otp.util.PasswordUtil;

public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void register(RegisterRequest request) {
        validateRegisterRequest(request);

        Role role = parseRole(request.getRole());

        User existingUser = userDao.findByLogin(request.getLogin());
        if (existingUser != null) {
            throw new IllegalArgumentException("User already exists");
        }

        if (role == Role.ADMIN && userDao.adminExists()) {
            throw new IllegalArgumentException("Admin already exists");
        }

        String passwordHash = PasswordUtil.hash(request.getPassword());

        userDao.create(request.getLogin(), passwordHash, role);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}