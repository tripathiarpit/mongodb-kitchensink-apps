package com.mongodb.kitchensink.dto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static com.mongodb.kitchensink.constants.ErrorMessageConstants.*;
public class AddressRequestDtoTest {
    private Validator validator;
    private AddressRequest addressRequest;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        addressRequest = new AddressRequest();
    }

    private AddressRequest createValidAddress() {
        AddressRequest address = new AddressRequest();
        address.setStreet("123 Main St");
        address.setCity("Anytown");
        address.setState("State");
        address.setPincode("12345");
        address.setCountry("USA");
        return address;
    }

    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        assertNotNull(addressRequest);
    }

    @Test
    @DisplayName("Test getters and setters")
    public void testGettersAndSetters() {
        String street = "123 Main St";
        String city = "Anytown";
        String state = "State";
        String pincode = "12345";
        String country = "USA";

        addressRequest.setStreet(street);
        addressRequest.setCity(city);
        addressRequest.setState(state);
        addressRequest.setPincode(pincode);
        addressRequest.setCountry(country);

        assertEquals(street, addressRequest.getStreet());
        assertEquals(city, addressRequest.getCity());
        assertEquals(state, addressRequest.getState());
        assertEquals(pincode, addressRequest.getPincode());
        assertEquals(country, addressRequest.getCountry());
    }

    @Test
    @DisplayName("Test valid address request validation")
    public void testValidAddress() {
        AddressRequest address = createValidAddress();
        Set violations = validator.validate(address);
        assertTrue(violations.isEmpty(), "Valid address should have no violations");
    }

    @Test
    @DisplayName("Test street field validation")
    public void testStreetValidation() {
        AddressRequest address = createValidAddress();
        address.setStreet(null);
        assertEquals(STREET_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setStreet("");
        assertEquals(STREET_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setStreet("a".repeat(101));
        assertEquals("Street must be at most 100 characters", validator.validate(address).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test city field validation")
    public void testCityValidation() {
        AddressRequest address = createValidAddress();
        address.setCity(null);
        assertEquals(CITY_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setCity("");
        assertEquals(CITY_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setCity("a".repeat(51));
        assertEquals("City must be at most 50 characters", validator.validate(address).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test state field validation")
    public void testStateValidation() {
        AddressRequest address = createValidAddress();
        address.setState(null);
        assertEquals(STATE_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setState("");
        assertEquals(STATE_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setState("a".repeat(51));
        assertEquals("State must be at most 50 characters", validator.validate(address).iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test pincode field validation")
    public void testPincodeValidation() {
        AddressRequest address = createValidAddress();
        address.setPincode(null);

        HashSet<String> violationMessagesForNull = (HashSet<String>) validator.validate(address).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        violationMessagesForNull.forEach(message -> System.out.println("***********"+message+
                violationMessagesForNull.contains(PINCODE_REQUIRED)+ "ANNDDDDD"+ violationMessagesForNull.contains(PINCODE_INVALID)));

        assertTrue(violationMessagesForNull.contains(PINCODE_REQUIRED));

        address.setPincode("");
        HashSet<String> violationMessagesForBlank = (HashSet<String>) validator.validate(address).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violationMessagesForBlank.contains(PINCODE_REQUIRED));


        address.setPincode("12");
        HashSet<String> violationMessagesForShort = (HashSet<String>) validator.validate(address).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violationMessagesForShort.contains(PINCODE_INVALID));

        address.setPincode("invalid@!");
        HashSet<String> violationMessagesForInvalid = (HashSet<String>) validator.validate(address).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violationMessagesForInvalid.contains(PINCODE_INVALID));
    }

    @Test
    @DisplayName("Test country field validation")
    public void testCountryValidation() {
        AddressRequest address = createValidAddress();
        address.setCountry(null);
        assertEquals(COUNTRY_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setCountry("");
        assertEquals(COUNTRY_REQUIRED, validator.validate(address).iterator().next().getMessage());

        address.setCountry("a".repeat(51));
        assertEquals("Country must be at most 50 characters", validator.validate(address).iterator().next().getMessage());
    }
}
