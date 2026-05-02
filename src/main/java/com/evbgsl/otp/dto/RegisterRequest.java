package com.evbgsl.otp.dto;

public class RegisterRequest {

    private String login;
    private String password;
    private String role;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}