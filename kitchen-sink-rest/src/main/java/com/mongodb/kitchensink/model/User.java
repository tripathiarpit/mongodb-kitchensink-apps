package com.mongodb.kitchensink.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Document(collection = "users")
public class User {
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", roles=" + roles +
                ", active=" + active +
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

    private List<String> roles; // e.g. ["USER", "ADMIN"]

    private boolean active = true;

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
        return email; // fallback if no '@' found
    }

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

        public Builder id(String id) {
            this.id = id;
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

        public User build() {
            User user = new User();
            user.id = this.id;
            user.email = this.email;
            user.username = this.username;
            user.passwordHash = this.passwordHash;
            user.roles = this.roles;
            user.active = this.active;
            user.createdAt = this.createdAt;
            return user;
        }

        // Static helper for builder
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
                    '}';
        }
    }
}

