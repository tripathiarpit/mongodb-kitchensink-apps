package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;

public class JwtExpiredException extends RuntimeException {
    private final ErrorCodes errorCode;
    public JwtExpiredException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public JwtExpiredException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}