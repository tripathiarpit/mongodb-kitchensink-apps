package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Response returned after user registration")
public class RegistrationResponse {
    public RegistrationResponse(boolean success, String message) {
        this.success = success;
        this.registrationMessage = message;
    }

    @Schema(description = "Response message", example = "User registered successfully")
    private String registrationMessage;

    @Schema(description = "Whether the registration was successful", example = "true")
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRegistrationMessage() {
        return registrationMessage;
    }

    public void setRegistrationMessage(String registrationMessage) {
        this.registrationMessage = registrationMessage;
    }
}
