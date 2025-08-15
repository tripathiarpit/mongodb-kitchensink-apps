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

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDeniedDetailed(
            AuthorizationDeniedException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", 403);

        String message = "Access denied. ";
        if (ex.getMessage().contains("ADMIN")) {
            message += "Admin privileges required.";
        } else {
            message += "Insufficient permissions.";
        }

        body.put("message", message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

}
