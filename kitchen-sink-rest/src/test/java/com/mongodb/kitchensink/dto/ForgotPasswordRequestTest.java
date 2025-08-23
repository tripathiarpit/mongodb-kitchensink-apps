package com.mongodb.kitchensink.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordRequestTest {

    private Validator validator;
    private ForgotPasswordRequest forgotPasswordRequest;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        forgotPasswordRequest = new ForgotPasswordRequest();
    }

    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        assertNotNull(forgotPasswordRequest);
    }

    @Test
    @DisplayName("Test getters and setters")
    public void testGettersAndSetters() {
        String email = "test@example.com";
        forgotPasswordRequest.setEmail(email);
        assertEquals(email, forgotPasswordRequest.getEmail());
    }

    @Test
    @DisplayName("Test valid email format")
    public void testValidEmail() {
        forgotPasswordRequest.setEmail("valid.email@example.com");
        assertTrue(validator.validate(forgotPasswordRequest).isEmpty());
    }

    @Test
    @DisplayName("Test blank email")
    public void testBlankEmail() {
        forgotPasswordRequest.setEmail("");
        assertEquals("Email cannot be blank", validator.validate(forgotPasswordRequest).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test invalid email format")
    public void testInvalidEmailFormat() {
        forgotPasswordRequest.setEmail("invalid-email");
        assertEquals("Invalid email format", validator.validate(forgotPasswordRequest).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test email without domain")
    public void testEmailWithoutDomain() {
        forgotPasswordRequest.setEmail("user@");
        assertEquals("Invalid email format", validator.validate(forgotPasswordRequest).iterator().next().getMessage());
    }
}