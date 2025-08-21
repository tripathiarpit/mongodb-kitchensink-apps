package com.mongodb.kitchensink.dto;

import java.time.Instant;
import java.util.List;

public class UserDto {
    private String id;
    private String email;
    private String username;
    private List<String> roles;
    private boolean active;
    private Instant createdAt;
    private Boolean isAccountVerificationPending;
    private Boolean isFirstLogin;
    private String twoFactorSecret;
    private boolean twoFactorEnabled = false;
    public UserDto() {

    }
    public UserDto(String id, String email, String username, List<String> roles, boolean active, Instant createdAt, Boolean isAccountVerificationPending, Boolean isFirstLogin, String twoFactorSecret, boolean twoFactorEnabled, ProfileDto profile) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.active = active;
        this.createdAt = createdAt;
        this.isAccountVerificationPending = isAccountVerificationPending;
        this.isFirstLogin = isFirstLogin;
        this.twoFactorSecret = twoFactorSecret;
        this.twoFactorEnabled = twoFactorEnabled;
        this.profile = profile;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public Boolean getFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(Boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    public Boolean getAccountVerificationPending() {
        return isAccountVerificationPending;
    }

    public void setAccountVerificationPending(Boolean accountVerificationPending) {
        isAccountVerificationPending = accountVerificationPending;
    }

    private ProfileDto profile;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ProfileDto getProfile() {
        return profile;
    }

    public void setProfile(ProfileDto profile) {
        this.profile = profile;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
}