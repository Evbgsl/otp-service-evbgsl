package com.evbgsl.otp.service;

import com.evbgsl.otp.dao.OtpConfigDao;
import com.evbgsl.otp.dao.UserDao;
import com.evbgsl.otp.dto.OtpConfigUpdateRequest;
import com.evbgsl.otp.dto.UserResponse;
import com.evbgsl.otp.model.User;

import java.util.List;

public class AdminService {

    private final UserDao userDao;
    private final OtpConfigDao otpConfigDao;

    public AdminService(UserDao userDao, OtpConfigDao otpConfigDao) {
        this.userDao = userDao;
        this.otpConfigDao = otpConfigDao;
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userDao.findAllUsers();

        return users.stream()
                .map(UserResponse::new)
                .toList();
    }

    public void updateOtpConfig(OtpConfigUpdateRequest request) {
        validateOtpConfig(request);
        otpConfigDao.updateConfig(request.getCodeLength(), request.getTtlSeconds());
    }

    public void deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }

        boolean deleted = userDao.deleteById(id);

        if (!deleted) {
            throw new IllegalArgumentException("User not found");
        }
    }

    private void validateOtpConfig(OtpConfigUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (request.getCodeLength() < 4 || request.getCodeLength() > 10) {
            throw new IllegalArgumentException("Code length must be between 4 and 10");
        }

        if (request.getTtlSeconds() < 30 || request.getTtlSeconds() > 3600) {
            throw new IllegalArgumentException("TTL seconds must be between 30 and 3600");
        }
    }
}