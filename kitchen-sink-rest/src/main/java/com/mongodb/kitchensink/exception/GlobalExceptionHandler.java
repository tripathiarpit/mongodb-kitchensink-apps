package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ------------------ Specific Exceptions ------------------

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorCodes errorCode = ErrorCodes.RESOURCE_NOT_FOUND;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(
                        ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage(),
                        errorCode.getStatus()
                ));
    }

    @ExceptionHandler(UserAuthException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthException(UserAuthException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleOtpException(InvalidOtpException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JwtExpiredException.class)
    public ResponseEntity<ErrorResponse> handleJwtExpired(JwtExpiredException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ------------------ Validation Exceptions ------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((FieldError error) ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(AccountVerificationException.class)
    public ResponseEntity<String> handleVerificationPending(AccountVerificationException ex) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus()) // HttpStatus.BAD_REQUEST
                .body(new ErrorResponse(ex.getMessage(), ex.getErrorCode().getStatus()));
    }

    // ------------------ Authorization Exceptions ------------------

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 403);

        String message = "Access denied.";
        if (ex.getMessage() != null && ex.getMessage().contains("ADMIN")) {
            message += " Admin privileges required.";
        } else {
            message += " Insufficient permissions.";
        }

        body.put("message", message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ------------------ Generic Exceptions ------------------

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorCodes errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(ex.getMessage(), errorCode.getStatus()));
    }
}
