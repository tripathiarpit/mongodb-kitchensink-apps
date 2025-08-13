package com.mongodb.kitchensink.dto;

import java.util.List;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String email;
    private String username;
    private List<String> roles;

    public LoginResponse(boolean success, String message, String token, String email, String username, List<String> roles) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.email = email;
        this.username = username;
        this.roles = roles;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
