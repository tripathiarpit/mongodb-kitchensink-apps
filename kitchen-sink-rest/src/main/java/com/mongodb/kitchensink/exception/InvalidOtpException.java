package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;

public class InvalidOtpException extends RuntimeException {
    private final ErrorCodes errorCode;

    public InvalidOtpException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public InvalidOtpException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    public InvalidOtpException(String customMessage, ErrorCodes errorCode) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}
