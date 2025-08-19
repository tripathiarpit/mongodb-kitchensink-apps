package com.mongodb.kitchensink.config;

import org.springframework.context.annotation.Configuration;


public class JwtConfig {
    private String secret;
    private long expirationMs;

    public JwtConfig() {
    }

    public JwtConfig(String secret, long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    @Override
    public String toString() {
        return "JwtConfig{" +
                "secret='[PROTECTED]'" + // Avoid logging actual secret
                ", expirationMs=" + expirationMs +
                '}';
    }
}
