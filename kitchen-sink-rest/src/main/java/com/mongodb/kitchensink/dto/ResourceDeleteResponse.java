package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;

    public class ResourceDeleteResponse {
        public ResourceDeleteResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

    @Schema(description = "Response message", example = "Resource Deleted Successfully")
    private String message;

    @Schema(description = "Resource Deleted is Success", example = "true")
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
