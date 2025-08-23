package com.mongodb.kitchensink.dto;

import java.util.List;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String email;
    private String username;
    private String fullName;
    private List<String> roles;
    private boolean accountVerificationPending;
    private boolean isFirstLogin;

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    public LoginResponse(boolean success, String message, String token, String email, String username, String fullname, List<String> roles, boolean accountVerificationPending, boolean isFirstLogin) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.email = email;
        this.username = username;
        this.fullName = fullname;
        this.roles = roles;
        this.accountVerificationPending = accountVerificationPending;
        this.isFirstLogin = isFirstLogin;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAccountVerificationPending() {
        return accountVerificationPending;
    }

    public void setAccountVerificationPending(boolean accountVerificationPending) {
        this.accountVerificationPending = accountVerificationPending;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", roles=" + roles +
                ", accountVerificationPending=" + accountVerificationPending +
                ", isFirstLogin=" + isFirstLogin +
                '}';
    }
}
