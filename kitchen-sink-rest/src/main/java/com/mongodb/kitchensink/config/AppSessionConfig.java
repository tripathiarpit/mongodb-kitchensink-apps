package com.mongodb.kitchensink.config;


public class AppSessionConfig {
    private long expirationSeconds; // Session expiration in seconds

    public AppSessionConfig() {
    }

    public AppSessionConfig(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    // Getters and Setters
    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String toString() {
        return "AppSessionConfig{" +
                "expirationSeconds=" + expirationSeconds +
                '}';
    }
}
