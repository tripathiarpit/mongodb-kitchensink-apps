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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationRequestTest {

    private Validator validator;
    private RegistrationRequest registrationRequest;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        registrationRequest = new RegistrationRequest();
    }

    private RegistrationRequest createValidRegistrationRequest() {
        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("P@ssw0rd123");
        request.setPhoneNumber("1234567890");
        request.setCity("Anytown");
        request.setPincode("12345");
        request.setAddress(new AddressRequest());
        return request;
    }

    @Test
    @DisplayName("Test valid registration request")
    public void testValidRegistrationRequest() {
        RegistrationRequest request = createValidRegistrationRequest();
        Set violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    @DisplayName("Test getters and setters")
    public void testGettersAndSetters() {
        registrationRequest.setFirstName("Jane");
        assertEquals("Jane", registrationRequest.getFirstName());

        registrationRequest.setLastName("Smith");
        assertEquals("Smith", registrationRequest.getLastName());

        registrationRequest.setEmail("jane.smith@example.com");
        assertEquals("jane.smith@example.com", registrationRequest.getEmail());

        registrationRequest.setPassword("securepass");
        assertEquals("securepass", registrationRequest.getPassword());

        registrationRequest.setPhoneNumber("0987654321");
        assertEquals("0987654321", registrationRequest.getPhoneNumber());

        registrationRequest.setCity("Othertown");
        assertEquals("Othertown", registrationRequest.getCity());

        registrationRequest.setPincode("54321");
        assertEquals("54321", registrationRequest.getPincode());

        AddressRequest address = new AddressRequest();
        registrationRequest.setAddress(address);
        assertEquals(address, registrationRequest.getAddress());

        List<String> roles = Collections.singletonList("USER");
        registrationRequest.setRoles(roles);
        assertEquals(roles, registrationRequest.getRoles());
    }

    @Test
    @DisplayName("Test first name validation")
    public void testFirstNameValidation() {
        RegistrationRequest request = createValidRegistrationRequest();
        request.setFirstName(null);
        assertEquals("First name is required", validator.validate(request).iterator().next().getMessage());
        request.setFirstName("");
        assertEquals("First name is required", validator.validate(request).iterator().next().getMessage());
        request.setFirstName("a".repeat(51));
        assertEquals("First name must be at most 50 characters", validator.validate(request).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test last name validation")
    public void testLastNameValidation() {
        RegistrationRequest request = createValidRegistrationRequest();
        request.setLastName(null);
        assertEquals("Last name is required", validator.validate(request).iterator().next().getMessage());
        request.setLastName("");
        assertEquals("Last name is required", validator.validate(request).iterator().next().getMessage());
        request.setLastName("a".repeat(51));
        assertEquals("Last name must be at most 50 characters", validator.validate(request).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test email validation")
    public void testEmailValidation() {
        RegistrationRequest request = createValidRegistrationRequest();
        request.setEmail(null);
        assertEquals("Email is required", validator.validate(request).iterator().next().getMessage());
        request.setEmail("");
        assertEquals("Email is required", validator.validate(request).iterator().next().getMessage());
        request.setEmail("invalid-email");
        assertEquals("Email should be valid", validator.validate(request).iterator().next().getMessage());
    }

    @Test
    @DisplayName("testPasswordValidation should fail with correct message")
    void testPasswordValidation() {
        RegistrationRequest request = new RegistrationRequest();
        request.setPassword(""); // Empty password
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password must be at least 8 characters")));
    }

    @Test
    @DisplayName("testPhoneNumberValidation should fail with correct message")
    void testPhoneNumberValidation() {
        RegistrationRequest request = new RegistrationRequest();
        request.setPhoneNumber("");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Phone number is required")));
    }

    @Test
    @DisplayName("Test address validation")
    public void testAddressValidation() {
        RegistrationRequest request = createValidRegistrationRequest();
        request.setAddress(null);
        assertEquals("Address is required", validator.validate(request).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test city validation")
    public void testCityValidation() {
        RegistrationRequest request = createValidRegistrationRequest();
        request.setCity(null);
        assertEquals("City is required", validator.validate(request).iterator().next().getMessage());
        request.setCity("");
        assertEquals("City is required", validator.validate(request).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test pincode validation")
    public void testPincodeValidation() {
        // Scenario 1: Null pincode
        RegistrationRequest nullPincodeRequest = createValidRegistrationRequest();
        nullPincodeRequest.setPincode(null);

        Set<ConstraintViolation<RegistrationRequest>> violationsForNull = validator.validate(nullPincodeRequest);

        Set<String> violationMessagesForNull = violationsForNull.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violationMessagesForNull.contains("Pincode is required"));

        RegistrationRequest emptyPincodeRequest = createValidRegistrationRequest();
        emptyPincodeRequest.setPincode("");
        Set<ConstraintViolation<RegistrationRequest>> violationsForEmpty = validator.validate(emptyPincodeRequest);
        violationsForEmpty.forEach(violation -> System.err.println("*****ERRORS"+violation.getMessage()));
        assertEquals(2, violationsForEmpty.size());
        Set<String> violationMessagesForNullset = violationsForNull.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violationMessagesForNullset.contains("Pincode is required"));

        RegistrationRequest shortPincodeRequest = createValidRegistrationRequest();
        shortPincodeRequest.setPincode("12");
        Set<ConstraintViolation<RegistrationRequest>> violationsForShort = validator.validate(shortPincodeRequest);

        // For a short, non-blank string, only @Pattern is violated
        assertEquals(1, violationsForShort.size());
        assertEquals("Pincode must be 3-10 alphanumeric characters",violationsForShort.iterator().next().getMessage());
    }
}