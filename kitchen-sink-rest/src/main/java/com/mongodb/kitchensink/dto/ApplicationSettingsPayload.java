package com.mongodb.kitchensink.dto;

public class ApplicationSettingsPayload {
    private int sessionExpirySeconds;
    private int forgotPasswordOtpExpirySeconds;
    private int userRegistrationOtpExpirySeconds;

    public int getSessionExpirySeconds() {
        return sessionExpirySeconds;
    }

    public void setSessionExpirySeconds(int sessionExpirySeconds) {
        this.sessionExpirySeconds = sessionExpirySeconds;
    }

    public int getForgotPasswordOtpExpirySeconds() {
        return forgotPasswordOtpExpirySeconds;
    }

    public void setForgotPasswordOtpExpirySeconds(int forgotPasswordOtpExpirySeconds) {
        this.forgotPasswordOtpExpirySeconds = forgotPasswordOtpExpirySeconds;
    }

    public int getUserRegistrationOtpExpirySeconds() {
        return userRegistrationOtpExpirySeconds;
    }

    public void setUserRegistrationOtpExpirySeconds(int userRegistrationOtpExpirySeconds) {
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
