package com.evbgsl.otp.model;

public class User {

    private Long id;
    private String login;
    private String passwordHash;
    private Role role;

    public User(Long id, String login, String passwordHash, Role role) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}