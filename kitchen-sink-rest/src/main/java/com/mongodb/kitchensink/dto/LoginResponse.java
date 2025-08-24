package com.mongodb.kitchensink.dto;

import java.util.List;

public class LoginResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String email;
    private String username;
    private String fullName;
    private List<String> roles;
    private boolean accountVerificationPending;
    private boolean firstLogin;

    public LoginResponse(boolean success, String message, String accessToken, String refreshToken, String email,
                         String username, String fullName, List<String> roles, boolean accountVerificationPending,
                         boolean firstLogin) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
        this.accountVerificationPending = accountVerificationPending;
        this.firstLogin = firstLogin;
    }

    // Getters and Setters
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean getAccountVerificationPending() {
        return accountVerificationPending;
    }

    public void setAccountVerificationPending(boolean accountVerificationPending) {
        this.accountVerificationPending = accountVerificationPending;
    }

    public boolean getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }
}