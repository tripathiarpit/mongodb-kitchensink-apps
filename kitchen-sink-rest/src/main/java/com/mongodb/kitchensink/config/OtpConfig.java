package com.mongodb.kitchensink.config;

import org.springframework.context.annotation.Configuration;


public class OtpConfig {
    private Long accountVerificationTtlSeconds;
    private Long forgotPasswordTtlSeconds;
    private Long length;
    private Long expirationSeconds; // General OTP expiration for app usage

    public OtpConfig() {
    }

    public OtpConfig(Long accountVerificationTtlSeconds, Long forgotPasswordTtlSeconds, Long length, Long expirationSeconds) {
        this.accountVerificationTtlSeconds = accountVerificationTtlSeconds;
        this.forgotPasswordTtlSeconds = forgotPasswordTtlSeconds;
        this.length = length;
        this.expirationSeconds = expirationSeconds;
    }

    // Getters and Setters
    public Long getAccountVerificationTtlSeconds() {
        return accountVerificationTtlSeconds;
    }

    public void setAccountVerificationTtlSeconds(Long accountVerificationTtlSeconds) {
        this.accountVerificationTtlSeconds = accountVerificationTtlSeconds;
    }

    public Long getForgotPasswordTtlSeconds() {
        return forgotPasswordTtlSeconds;
    }

    public void setForgotPasswordTtlSeconds(Long forgotPasswordTtlSeconds) {
        this.forgotPasswordTtlSeconds = forgotPasswordTtlSeconds;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(Long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String toString() {
        return "OtpConfig{" +
                "accountVerificationTtlSeconds=" + accountVerificationTtlSeconds +
                ", forgotPasswordTtlSeconds=" + forgotPasswordTtlSeconds +
                ", length=" + length +
                ", expirationSeconds=" + expirationSeconds +
                '}';
    }
}
