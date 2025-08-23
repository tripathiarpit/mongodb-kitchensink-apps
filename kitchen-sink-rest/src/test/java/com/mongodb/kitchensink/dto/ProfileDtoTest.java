package com.mongodb.kitchensink.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class ProfileDtoTest {

    private ProfileDto profileDto;

    @BeforeEach
    public void setUp() {
        profileDto = new ProfileDto();
    }

    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        assertNotNull(profileDto);
        assertNull(profileDto.getFirstName());
        assertNull(profileDto.getLastName());
    }

    @Test
    @DisplayName("Test all-args constructor")
    public void testAllArgsConstructor() {
        String firstName = "John";
        String lastName = "Doe";
        String phoneNumber = "1234567890";
        String street = "123 Main St";
        String city = "Anytown";
        String state = "State";
        String country = "USA";
        String pincode = "12345";

        ProfileDto allArgsConstructorProfile = new ProfileDto(firstName, lastName, phoneNumber, street, city, state, country, pincode);

        assertEquals(firstName, allArgsConstructorProfile.getFirstName());
        assertEquals(lastName, allArgsConstructorProfile.getLastName());
        assertEquals(phoneNumber, allArgsConstructorProfile.getPhoneNumber());
        assertEquals(street, allArgsConstructorProfile.getStreet());
        assertEquals(city, allArgsConstructorProfile.getCity());
        assertEquals(state, allArgsConstructorProfile.getState());
        assertEquals(country, allArgsConstructorProfile.getCountry());
        assertEquals(pincode, allArgsConstructorProfile.getPincode());
    }

    @Test
    @DisplayName("Test getters and setters")
    public void testGettersAndSetters() {
        String firstName = "Jane";
        String lastName = "Smith";
        String phoneNumber = "0987654321";
        String street = "456 Oak Ave";
        String city = "Othertown";
        String state = "Other State";
        String country = "Canada";
        String pincode = "67890";

        profileDto.setFirstName(firstName);
        profileDto.setLastName(lastName);
        profileDto.setPhoneNumber(phoneNumber);
        profileDto.setStreet(street);
        profileDto.setCity(city);
        profileDto.setState(state);
        profileDto.setCountry(country);
        profileDto.setPincode(pincode);

        assertEquals(firstName, profileDto.getFirstName());
        assertEquals(lastName, profileDto.getLastName());
        assertEquals(phoneNumber, profileDto.getPhoneNumber());
        assertEquals(street, profileDto.getStreet());
        assertEquals(city, profileDto.getCity());
        assertEquals(state, profileDto.getState());
        assertEquals(country, profileDto.getCountry());
        assertEquals(pincode, profileDto.getPincode());
    }
}