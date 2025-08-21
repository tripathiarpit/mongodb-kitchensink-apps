package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("should handle UserNotFoundException and return NOT_FOUND status")
    void handleUserNotFound_shouldReturnNotFound() {
        // Given
        UserNotFoundException ex = new UserNotFoundException("User not found.", ErrorCodes.USER_NOT_FOUND);

        // When
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUserNotFound(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("User not found.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getBody().getStatus());
    }

    @Test
    @DisplayName("should handle ResourceNotFoundException and return NOT_FOUND status")
    void handleResourceNotFound_shouldReturnNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found.",ErrorCodes.USER_NOT_FOUND);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleResourceNotFound(ex);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Resource not found.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getBody().getStatus());
    }

    @Test
    @DisplayName("should handle UserAuthException and return UNAUTHORIZED status")
    void handleUserAuthException_shouldReturnUnauthorized() {
        // Given
        UserAuthException ex = new UserAuthException("Invalid credentials.", ErrorCodes.INVALID_CREDENTIALS);

        // When
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUserAuthException(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Invalid credentials.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getBody().getStatus());
    }

    @Test
    @DisplayName("should handle InvalidOtpException and return BAD_REQUEST status")
    void handleOtpException_shouldReturnBadRequest() {
        // Given
        InvalidOtpException ex = new InvalidOtpException("Invalid OTP.", ErrorCodes.INVALID_OTP);

        // When
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleOtpException(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Invalid OTP.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getBody().getStatus());
    }

    @Test
    @DisplayName("should handle JwtExpiredException and return BAD_REQUEST status")
    void handleJwtExpired_shouldReturnBadRequest() {
        // Given
        JwtExpiredException ex = new JwtExpiredException("JWT token has expired.", ErrorCodes.INVALID_JWT_TOKEN);

        // When
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleJwtExpired(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("JWT token has expired.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getBody().getStatus());
    }

    @Test
    @DisplayName("should handle InvalidRequestException and return BAD_REQUEST status")
    void handleInvalidRequest_shouldReturnBadRequest() {
        // Given
        InvalidRequestException ex = new InvalidRequestException("Invalid request parameters.", ErrorCodes.INVALID_REQUEST);

        // When
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleInvalidRequest(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Invalid request parameters.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getBody().getStatus());
    }

    @Test
    @DisplayName("should handle MethodArgumentNotValidException and return a map of validation errors")
    void handleValidationExceptions_shouldReturnBadRequestWithErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("objectName", "field1", "Error message for field1");
        FieldError fieldError2 = new FieldError("objectName", "field2", "Error message for field2");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, String>> responseEntity = globalExceptionHandler.handleValidationExceptions(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        assertEquals("Error message for field1", responseEntity.getBody().get("field1"));
        assertEquals("Error message for field2", responseEntity.getBody().get("field2"));
    }

    @Test
    @DisplayName("should handle AccountVerificationExcpetion and return BAD_REQUEST with message")
    void handleVerificationPending_shouldReturnBadRequest() {
        // Given
        AccountVerificationException ex = new AccountVerificationException(ErrorCodes.ACCOUNT_VERIFICATION_PENDING);

        // When
        ResponseEntity<String> responseEntity = globalExceptionHandler.handleVerificationPending(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Account verification is pending, verification requesting OTP.", responseEntity.getBody());
    }

    @Test
    @DisplayName("should handle BadRequestException and return BAD_REQUEST status")
    void handleBadRequest_shouldReturnBadRequest() {
        // Given
        BadRequestException ex = new BadRequestException("Bad request format.", ErrorCodes.INVALID_REQUEST);

        // When
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleBadRequest(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Bad request format.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getBody().getStatus());
    }

    // Similarly, correct the other test:
    @Test
    @DisplayName("should handle AuthorizationDeniedException for admin privileges required")
    void handleAuthorizationDenied_shouldReturnForbiddenWithAdminMessage() {
        // Correct setup: Mock AuthorizationResult to be DENIED
        AuthorizationResult deniedResult = mock(AuthorizationResult.class);
        when(deniedResult.isGranted()).thenReturn(false);

        // Correct the exception message to include "ADMIN" in uppercase
        AuthorizationDeniedException ex = new AuthorizationDeniedException("Authorization Denied: ADMIN role required.", deniedResult);

        // When
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleAuthorizationDenied(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().get("status"));
        assertEquals("Access denied. Admin privileges required.", responseEntity.getBody().get("message"));
    }


    @Test
    @DisplayName("should handle RuntimeException and return INTERNAL_SERVER_ERROR status")
    void handleRuntimeException_shouldReturnInternalServerError() {
        // Given
        RuntimeException ex = new RuntimeException("A generic runtime error occurred.");

        // When
        ResponseEntity<String> responseEntity = globalExceptionHandler.handleRuntimeException(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("A generic runtime error occurred.", responseEntity.getBody());
    }

    @Test
    @DisplayName("should handle global Exception and return INTERNAL_SERVER_ERROR status")
    void handleGlobalException_shouldReturnInternalServerError() {
        // Given
        Exception ex = new Exception("An unexpected error occurred.");

        // When
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleGlobalException(ex);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("An unexpected error occurred.", responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getBody().getStatus());
    }
}