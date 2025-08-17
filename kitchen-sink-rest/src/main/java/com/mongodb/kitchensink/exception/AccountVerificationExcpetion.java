package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;

public class AccountVerificationExcpetion extends RuntimeException {
    private final ErrorCodes errorCode;

    public AccountVerificationExcpetion(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AccountVerificationExcpetion(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}
