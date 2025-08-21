package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;

public class UserAuthException extends RuntimeException {
    private final ErrorCodes errorCode;

    public UserAuthException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public UserAuthException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    public UserAuthException(String customMessage, ErrorCodes errorCode) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}