package com.mongodb.kitchensink.dto;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.SuccessMessageConstants;

public class ApiResponse {
    private boolean success;
    private ErrorCodes code;
    private String message;
    public ApiResponse() {

    }

    public ApiResponse( String message, boolean success) {
        this.success = success;
        this.message = message;
    }
    public ApiResponse( ErrorCodes code, boolean success) {
        this.success = success;
        this.code = code;
    }



    public static ApiResponse success(String message) {
        ApiResponse res = new ApiResponse();
        res.success = true;
        res.message = message;
        return res;
    }

    public static ApiResponse error(ErrorCodes errorCode, String message) {
        ApiResponse res = new ApiResponse();
        res.success = false;
        res.code = errorCode;
        res.message = message;
        return res;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorCodes getCode() {
        return code;
    }

    public void setCode(ErrorCodes code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
