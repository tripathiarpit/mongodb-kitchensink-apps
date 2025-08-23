package com.mongodb.kitchensink.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "users")
public class User implements UserDetails {
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", isAccountVerificationPending=" + isAccountVerificationPending +
                ", roles=" + roles +
                ", active=" + active +
                ", isFirstLogin=" + isFirstLogin +
                ", twoFactorSecret='" + twoFactorSecret + '\'' +
                ", twoFactorEnabled=" + twoFactorEnabled +
                ", createdAt=" + createdAt +
                '}';
    }

    @Id
    private String id;

    @NotNull
    @Email
    @Indexed(unique = true)
    private String email;

    // New field
    @NotNull
    @Indexed(unique = true)
    private String username;

    @NotNull
    private String passwordHash;
    private Boolean isAccountVerificationPending= true;
    private List<String> roles;
    private boolean active = true;

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    private Boolean isFirstLogin = true;
    private String twoFactorSecret;
    private boolean twoFactorEnabled = false;
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

    private Instant createdAt = Instant.now();

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.username = extractUsernameFromEmail(email);
    }

    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Helper method to extract username before '@'
    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return email;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }


    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String email;
        private String username;
        private String passwordHash;
        private List<String> roles;
        private boolean active;
        private Instant createdAt;
        private Boolean isAccountVerificationPending;
        private Boolean isFirstLogin;
        private  String twoFactorSecret;
        private Boolean twoFactorEnabled;

        public Builder twoFactorSecret(String twoFactorSecret) {
            this.twoFactorSecret = twoFactorSecret;
            return this;
        }
        public Builder twoFactorEnabled(boolean twoFactorEnabled) {
            this.twoFactorEnabled = twoFactorEnabled;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            this.username = extractUsernameFromEmailStatic(email);
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public Builder accountVerificationPending(boolean status) {
            this.isAccountVerificationPending = status;
            return this;
        }
        public Builder isFirstLogin(boolean status) {
            this.isFirstLogin = status;
            return this;
        }




        public User build() {
            User user = new User();
            user.id = this.id;
            user.email = this.email;
            user.username = this.username;
            user.passwordHash = this.passwordHash;
            user.roles = this.roles;
            user.active = this.active;
            user.createdAt = this.createdAt;
            user.isAccountVerificationPending = this.isAccountVerificationPending;
            user.isFirstLogin = this.isFirstLogin;
            user.twoFactorEnabled = this.twoFactorEnabled;
            user.twoFactorSecret = this.twoFactorSecret;
            return user;
        }

        private static String extractUsernameFromEmailStatic(String email) {
            if (email != null && email.contains("@")) {
                return email.substring(0, email.indexOf("@"));
            }
            return email;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "id='" + id + '\'' +
                    ", email='" + email + '\'' +
                    ", username='" + username + '\'' +
                    ", passwordHash='" + passwordHash + '\'' +
                    ", roles=" + roles +
                    ", active=" + active +
                    ", createdAt=" + createdAt +
                    ", isAccountVerificationPending=" + isAccountVerificationPending +
                    ", isFirstLogin=" + isFirstLogin +
                    ", twoFactorSecret='" + twoFactorSecret + '\'' +
                    ", twoFactorEnabled=" + twoFactorEnabled +
                    '}';
        }
    }
}

