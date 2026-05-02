package com.evbgsl.otp.dto;

import com.evbgsl.otp.model.User;

public class UserResponse {

    private final Long id;
    private final String login;
    private final String role;

    public UserResponse(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.role = user.getRole().name();
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }
}