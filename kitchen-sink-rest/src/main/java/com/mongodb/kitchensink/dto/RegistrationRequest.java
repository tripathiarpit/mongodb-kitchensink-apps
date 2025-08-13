package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
@Schema(description = "User registration request payload")
public class RegistrationRequest {

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Email address (must be unique)", example = "john@example.com")
    private String email;

    @Schema(description = "Password (will be encrypted)", example = "P@ssw0rd")
    private String password;

    @Schema(description = "Phone number", example = "9876543210")
    private String phoneNumber;

    @NotNull(message = "Address is required")
    private AddressRequest address;

    @Schema(description = "City name", example = "New York")
    private String city;

    @Schema(description = "Postal code", example = "10001")
    private String pincode;

    @Schema(description = "List of roles assigned to the user", example = "[\"USER\"]")
    private List<String> roles;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AddressRequest getAddress() {
        return address;
    }

    public void setAddress(AddressRequest address) {
        this.address = address;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
