package com.mongodb.kitchensink.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Profile Model Tests")
class ProfileTest {

    private Validator validator;

    private static final String ID = "profile123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String PHONE_NUMBER = "123-456-7890";
    private static final String USERNAME = "johndoe";
    private static final Address ADDRESS = new Address();

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should create Profile with default constructor and have null fields")
    void testNoArgsConstructor() {
        // When
        Profile profile = new Profile();

        // Then
        assertNull(profile.getId());
        assertNull(profile.getFirstName());
        assertNull(profile.getLastName());
        assertNull(profile.getEmail());
        assertNull(profile.getPhoneNumber());
        assertNull(profile.getAddress());
        assertNull(profile.getUsername());
    }

    @Test
    @DisplayName("should create Profile with all-args constructor and correctly set fields")
    void testAllArgsConstructor() {
        // When
        Profile profile = new Profile(ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, ADDRESS, USERNAME);

        // Then
        assertEquals(ID, profile.getId());
        assertEquals(FIRST_NAME, profile.getFirstName());
        assertEquals(LAST_NAME, profile.getLastName());
        assertEquals(EMAIL, profile.getEmail());
        assertEquals(PHONE_NUMBER, profile.getPhoneNumber());
        assertEquals(ADDRESS, profile.getAddress());
        assertEquals(USERNAME, profile.getUsername());
    }

    @Test
    @DisplayName("should set and get all fields correctly")
    void testSettersAndGetters() {
        // Given
        Profile profile = new Profile();

        // When
        profile.setId(ID);
        profile.setFirstName(FIRST_NAME);
        profile.setLastName(LAST_NAME);
        profile.setEmail(EMAIL);
        profile.setPhoneNumber(PHONE_NUMBER);
        profile.setAddress(ADDRESS);
        profile.setUsername(USERNAME);

        // Then
        assertEquals(ID, profile.getId());
        assertEquals(FIRST_NAME, profile.getFirstName());
        assertEquals(LAST_NAME, profile.getLastName());
        assertEquals(EMAIL, profile.getEmail());
        assertEquals(PHONE_NUMBER, profile.getPhoneNumber());
        assertEquals(ADDRESS, profile.getAddress());
        assertEquals(USERNAME, profile.getUsername());
    }

    @Test
    @DisplayName("should build a Profile object correctly using the builder pattern")
    void testBuilder() {
        // When
        Profile profile = Profile.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .phoneNumber(PHONE_NUMBER)
                .address(ADDRESS)
                .username(USERNAME)
                .build();

        // Then
        assertEquals(ID, profile.getId());
        assertEquals(FIRST_NAME, profile.getFirstName());
        assertEquals(LAST_NAME, profile.getLastName());
        assertEquals(EMAIL, profile.getEmail());
        assertEquals(PHONE_NUMBER, profile.getPhoneNumber());
        assertEquals(ADDRESS, profile.getAddress());
        assertEquals(USERNAME, profile.getUsername());
    }

    @Test
    @DisplayName("should pass validation for a valid Profile object")
    void testValidation_validProfile() {
        // Given
        Profile profile = Profile.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertTrue(violations.isEmpty(), "Expected no validation violations for a valid profile");
    }

    @Test
    @DisplayName("should fail validation for null firstName")
    void testValidation_nullFirstName() {
        // Given
        Profile profile = Profile.builder()
                .firstName(null)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation for invalid firstName pattern")
    void testValidation_invalidFirstNamePattern() {
        // Given
        Profile profile = Profile.builder()
                .firstName("John123")
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Name must contain only letters and spaces", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation for null lastName")
    void testValidation_nullLastName() {
        // Given
        Profile profile = Profile.builder()
                .firstName(FIRST_NAME)
                .lastName(null)
                .email(EMAIL)
                .build();

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation for invalid lastName pattern")
    void testValidation_invalidLastNamePattern() {
        // Given
        Profile profile = Profile.builder()
                .firstName(FIRST_NAME)
                .lastName("Doe123!")
                .email(EMAIL)
                .build();

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Name must contain only letters and spaces", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation for null email")
    void testValidation_nullEmail() {
        // Given
        Profile profile = Profile.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(null)
                .build();

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation for invalid email format")
    void testValidation_invalidEmailFormat() {
        // Given
        Profile profile = Profile.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email("invalid-email")
                .build();

        // When
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("must be a well-formed email address", violations.iterator().next().getMessage());
    }
}