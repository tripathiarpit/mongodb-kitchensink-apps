package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorCodes errorCode = ErrorCodes.RESOURCE_NOT_FOUND;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage(), errorCode.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessages = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            errorMessages.append(field).append(": ").append(message).append("; ");
        });

        ErrorCodes errorCode = ErrorCodes.VALIDATION_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorMessages.toString(), errorCode.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorCodes errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getMessage(), errorCode.getStatus()));
    }
    @ExceptionHandler(UserAuthException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthException(UserAuthException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus());// or whatever fits
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleOtpException(InvalidOtpException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus());// or whatever fits
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
