package com.mongodb.kitchensink.dto;

public class ApplicationSettingsPayload {
    private Long sessionExpirySeconds;
    private Long forgotPasswordOtpExpirySeconds;
    private Long userRegistrationOtpExpirySeconds;

    public Long getSessionExpirySeconds() {
        return sessionExpirySeconds;
    }

    public void setSessionExpirySeconds(Long sessionExpirySeconds) {
        this.sessionExpirySeconds = sessionExpirySeconds;
    }

    public Long getForgotPasswordOtpExpirySeconds() {
        return forgotPasswordOtpExpirySeconds;
    }

    public void setForgotPasswordOtpExpirySeconds(Long forgotPasswordOtpExpirySeconds) {
        this.forgotPasswordOtpExpirySeconds = forgotPasswordOtpExpirySeconds;
    }

    public Long getUserRegistrationOtpExpirySeconds() {
        return userRegistrationOtpExpirySeconds;
    }

    public void setUserRegistrationOtpExpirySeconds(Long userRegistrationOtpExpirySeconds) {
        this.userRegistrationOtpExpirySeconds = userRegistrationOtpExpirySeconds;
    }

    @Override
    public String toString() {
        return "ApplicationSettingsPayload{" +
                "sessionExpirySeconds=" + sessionExpirySeconds +
                ", forgotPasswordOtpExpirySeconds=" + forgotPasswordOtpExpirySeconds +
                ", userRegistrationOtpExpirySeconds=" + userRegistrationOtpExpirySeconds +
                '}';
    }
}
