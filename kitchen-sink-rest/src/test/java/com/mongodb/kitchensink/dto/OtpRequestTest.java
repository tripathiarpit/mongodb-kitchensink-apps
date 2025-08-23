package com.mongodb.kitchensink.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtpRequestTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Test case for a blank OTP
    @Test
    @DisplayName("should fail when OTP is blank")
    public void testBlankOtp() {
        OtpRequest otpRequest = new OtpRequest("test@example.com", "");
        var violations = validator.validate(otpRequest);

        // Assert that the message "OTP cannot be blank" is present
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("OTP cannot be blank")));
    }

    // Test case for an invalid OTP format
    @Test
    @DisplayName("should fail when OTP has an invalid format")
    public void testInvalidOtpFormat() {
        OtpRequest otpRequest = new OtpRequest("test@example.com", "12345"); // Not 6 digits
        var violations = validator.validate(otpRequest);

        // Assert that the message "OTP must be 6 digits" is present
        assertEquals(1, violations.size());
        assertEquals("OTP must be 6 digits", violations.iterator().next().getMessage());
    }

    // Test case for null OTP
    @Test
    @DisplayName("should fail when OTP is null")
    public void testNullOtp() {
        OtpRequest otpRequest = new OtpRequest("test@example.com", null);
        var violations = validator.validate(otpRequest);

        // Assert that the message "OTP cannot be blank" is present for a null value
        assertEquals(1, violations.size());
        assertEquals("OTP cannot be blank", violations.iterator().next().getMessage());
    }

}