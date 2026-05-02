package com.evbgsl.otp.service;

import com.evbgsl.otp.dao.UserDao;
import com.evbgsl.otp.dto.LoginRequest;
import com.evbgsl.otp.dto.LoginResponse;
import com.evbgsl.otp.dto.RegisterRequest;
import com.evbgsl.otp.model.Role;
import com.evbgsl.otp.model.User;
import com.evbgsl.otp.util.PasswordUtil;

public class AuthService {

    private final UserDao userDao;
    private final TokenService tokenService;

    public AuthService(UserDao userDao, TokenService tokenService) {
        this.userDao = userDao;
        this.tokenService = tokenService;
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

    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        User user = userDao.findByLogin(request.getLogin());

        if (user == null) {
            throw new IllegalArgumentException("Invalid login or password");
        }

        if (!PasswordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid login or password");
        }

        String token = tokenService.generateToken(user);

        return new LoginResponse(token, tokenService.getTokenTtlSeconds());
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

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
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