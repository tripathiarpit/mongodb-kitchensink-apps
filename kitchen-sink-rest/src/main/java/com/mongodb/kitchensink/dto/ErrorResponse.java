package com.mongodb.kitchensink.dto;
import org.springframework.http.HttpStatus;

public class ErrorResponse {
    private String message;
    private int status;

    public ErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
