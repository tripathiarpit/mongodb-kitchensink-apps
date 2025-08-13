package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static com.mongodb.kitchensink.constants.ErrorMessageConstants.*;


@Schema(description = "Address details for registration request")
public class AddressRequest {

    @NotBlank(message = STREET_REQUIRED)
    private String street;

    @NotBlank(message = CITY_REQUIRED)
    private String city;

    @NotBlank(message = STATE_REQUIRED)
    private String state;

    @NotBlank(message = PINCODE_REQUIRED)
    @Pattern(regexp = "\\d{5,6}", message = PINCODE_INVALID)
    private String pincode;

    @NotBlank(message = COUNTRY_REQUIRED)
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
