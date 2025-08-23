package com.mongodb.kitchensink.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ResetPasswordRequestTest {

    private Validator validator;
    private ResetPasswordRequest resetPasswordRequest;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        resetPasswordRequest = new ResetPasswordRequest();
    }

    private ResetPasswordRequest createValidRequest() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setNewPassword("SecurePass123");
        return request;
    }

    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        assertNotNull(resetPasswordRequest);
    }

    @Test
    @DisplayName("Test getters and setters")
    public void testGettersAndSetters() {
        String email = "test@domain.com";
        String password = "newPassword";

        resetPasswordRequest.setEmail(email);
        resetPasswordRequest.setNewPassword(password);

        assertEquals(email, resetPasswordRequest.getEmail());
        assertEquals(password, resetPasswordRequest.getNewPassword());
    }

    @Test
    @DisplayName("Test valid reset password request")
    public void testValidRequest() {
        ResetPasswordRequest request = createValidRequest();
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Test email field validation")
    public void testEmailValidation() {
        ResetPasswordRequest request = createValidRequest();
        request.setEmail(null);
        assertEquals("Email cannot be blank", validator.validate(request).iterator().next().getMessage());

        request.setEmail("");
        assertEquals("Email cannot be blank", validator.validate(request).iterator().next().getMessage());

        request.setEmail("invalid-email");
        assertEquals("Invalid email format", validator.validate(request).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test new password field validation")
    void testNewPasswordValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        // Case 1: Null password
        ResetPasswordRequest requestWithNullPassword = new ResetPasswordRequest();
        requestWithNullPassword.setEmail("Admin@example.com");
        requestWithNullPassword.setNewPassword(null);
        Set<ConstraintViolation<ResetPasswordRequest>> violationsForNull = validator.validate(requestWithNullPassword);
        Set<String> violationMessages = violationsForNull.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

         assertTrue(violationMessages.contains("New password cannot be blank"));

        // Case 2: Empty password
        ResetPasswordRequest requestWithEmptyPassword = new ResetPasswordRequest();
        requestWithEmptyPassword.setEmail("Admin@example.com");
        requestWithEmptyPassword.setNewPassword("");
        Set<ConstraintViolation<ResetPasswordRequest>> violationsForEmpty = validator.validate(requestWithEmptyPassword);
        Set<String> violationMessagess = violationsForEmpty.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violationMessages.contains("New password cannot be blank"));

        // Case 3: Short password
        ResetPasswordRequest requestWithShortPassword = new ResetPasswordRequest();
        requestWithShortPassword.setEmail("admin@example.com");
        requestWithShortPassword.setNewPassword("short");
        Set<ConstraintViolation<ResetPasswordRequest>> violationsForShort = validator.validate(requestWithShortPassword);
        Set<String> violationMessagess2 = violationsForEmpty.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertEquals(1, violationsForShort.size());
        violationMessagess2.forEach(System.out::println);
        assertTrue(violationMessages.contains("Password must be at least 8 characters long") ||
                violationMessages.contains("New password cannot be blank"));
    }
}
