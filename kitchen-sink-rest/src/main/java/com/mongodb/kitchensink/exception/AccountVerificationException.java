package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;

public class AccountVerificationException extends RuntimeException {
    private final ErrorCodes errorCode;

    public AccountVerificationException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AccountVerificationException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}
