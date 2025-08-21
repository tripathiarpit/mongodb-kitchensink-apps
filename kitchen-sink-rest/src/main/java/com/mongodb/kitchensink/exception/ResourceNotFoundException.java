package com.mongodb.kitchensink.exception;


import com.mongodb.kitchensink.constants.ErrorCodes;

public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCodes errorCode;

    public ResourceNotFoundException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ResourceNotFoundException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    public ResourceNotFoundException(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
