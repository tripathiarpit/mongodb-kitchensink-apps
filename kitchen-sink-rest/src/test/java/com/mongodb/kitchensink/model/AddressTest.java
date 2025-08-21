package com.mongodb.kitchensink.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Address Model Tests")
class AddressTest {

    private static final String STREET = "123 Main St";
    private static final String CITY = "Anytown";
    private static final String STATE = "CA";
    private static final String PINCODE = "12345";
    private static final String COUNTRY = "USA";

    @Test
    @DisplayName("should create Address with default constructor and have null fields")
    void testNoArgsConstructor() {
        // When
        Address address = new Address();

        // Then
        assertNull(address.getStreet());
        assertNull(address.getCity());
        assertNull(address.getState());
        assertNull(address.getPincode());
        assertNull(address.getCountry());
    }

    @Test
    @DisplayName("should create Address with all-args constructor and correctly set fields based on its specific order")
    void testAllArgsConstructor() {
        // When
        // The constructor's order is: (country, pincode, state, city, street)
        Address address = new Address(COUNTRY, PINCODE, STATE, CITY, STREET);

        // Then
        assertEquals(COUNTRY, address.getCountry());
        assertEquals(PINCODE, address.getPincode());
        assertEquals(STATE, address.getState());
        assertEquals(CITY, address.getCity());
        assertEquals(STREET, address.getStreet());
    }

    @Test
    @DisplayName("should set and get street correctly")
    void testSetAndGetStreet() {
        // Given
        Address address = new Address();

        // When
        address.setStreet(STREET);

        // Then
        assertEquals(STREET, address.getStreet());
    }

    @Test
    @DisplayName("should set and get city correctly")
    void testSetAndGetCity() {
        // Given
        Address address = new Address();

        // When
        address.setCity(CITY);

        // Then
        assertEquals(CITY, address.getCity());
    }

    @Test
    @DisplayName("should set and get state correctly")
    void testSetAndGetState() {
        // Given
        Address address = new Address();

        // When
        address.setState(STATE);

        // Then
        assertEquals(STATE, address.getState());
    }

    @Test
    @DisplayName("should set and get pincode correctly")
    void testSetAndGetPincode() {
        // Given
        Address address = new Address();

        // When
        address.setPincode(PINCODE);

        // Then
        assertEquals(PINCODE, address.getPincode());
    }

    @Test
    @DisplayName("should set and get country correctly")
    void testSetAndGetCountry() {
        // Given
        Address address = new Address();

        // When
        address.setCountry(COUNTRY);

        // Then
        assertEquals(COUNTRY, address.getCountry());
    }

    @Test
    @DisplayName("should build an Address object correctly using the builder pattern")
    void testBuilder() {
        // When
        Address address = Address.builder()
                .street(STREET)
                .city(CITY)
                .state(STATE)
                .pincode(PINCODE)
                .country(COUNTRY)
                .build();

        // Then
        assertEquals(STREET, address.getStreet());
        assertEquals(CITY, address.getCity());
        assertEquals(STATE, address.getState());
        assertEquals(PINCODE, address.getPincode());
        assertEquals(COUNTRY, address.getCountry());
    }
}