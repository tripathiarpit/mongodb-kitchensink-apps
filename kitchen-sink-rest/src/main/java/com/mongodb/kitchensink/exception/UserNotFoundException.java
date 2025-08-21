package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;

public class UserNotFoundException extends RuntimeException {
    private final ErrorCodes errorCode;

    public UserNotFoundException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public UserNotFoundException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    public UserNotFoundException(String customMessage, ErrorCodes errorCode) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}