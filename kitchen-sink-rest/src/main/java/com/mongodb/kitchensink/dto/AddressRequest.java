package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.mongodb.kitchensink.constants.ErrorMessageConstants.*;

@Schema(description = "Address details for registration request")
public class AddressRequest {

    @NotBlank(message = STREET_REQUIRED)
    @Size(max = 100, message = "Street must be at most 100 characters")
    private String street;

    @NotBlank(message = CITY_REQUIRED)
    @Size(max = 50, message = "City must be at most 50 characters")
    private String city;

    @NotBlank(message = STATE_REQUIRED)
    @Size(max = 50, message = "State must be at most 50 characters")
    private String state;

    @NotBlank(message = PINCODE_REQUIRED)
    @Pattern(regexp = "^[A-Za-z0-9\\s-]{3,10}$", message = PINCODE_INVALID)
    private String pincode;

    @NotBlank(message = COUNTRY_REQUIRED)
    @Size(max = 50, message = "Country must be at most 50 characters")
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
