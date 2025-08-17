package com.mongodb.kitchensink.constants;

import org.springframework.http.HttpStatus;
public enum ErrorCodes {
    RESOURCE_NOT_FOUND("Resource not found", HttpStatus.NO_CONTENT),
    VALIDATION_ERROR("Validation failed", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CREDENTIALS("Invalid email or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("User account is disabled", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("No account not found", HttpStatus.FORBIDDEN),
    ACCOUNT_VERIFICATION_FAILED("Account verification failed,You have entered Wrong or Expired OTP. Request a new OTP and try again.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_VERIFICATION_PENDING("Account verification is pending, verification requesting OTP.", HttpStatus.INTERNAL_SERVER_ERROR),
    SESSION_EXPIRED("Session has been expired, login again to continue", HttpStatus.FORBIDDEN);
    private final String message;
    private final HttpStatus status;

    ErrorCodes(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}