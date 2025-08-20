package com.mongodb.kitchensink.constants;
public class ErrorMessageConstants {
private ErrorMessageConstants() {
}
    public static final String REQ_PASSWORD = "Password is required";
    public static final String REQ_EMAIL = "Email is required";
    public static final String INVALID_REQUEST = "Request is invalid, try again";
    public static final String INVALID_EMAIL_FORMET = "Email in the request is not a valid email.";
    public static final String INVALID_OR_EXPIRED_SESSION = "Invalid or expired session";
    public static final String ACCOUNT_NOT_FOUND_EMAIL = "Account with this email does not exist";
    public static final String ACCOUNT_NOT_FOUND_ID = "Account with this ID does not exist";
    public static final String USER_NOT_FOUND_EMAIL = "User not found with email:";
    public static final String INVALID_OR_EXPIRED_OTP = "Invalid or expired OTP";
    public static final String FAILED_TO_SEND_EMAIL = "Failed to send email";
    public static final String TOKEN_EXPIRED = "Your session has expired. Please log in again to access the application.";
    public static final String STREET_REQUIRED = "Street is required";
    public static final String CITY_REQUIRED = "City is required";
    public static final String STATE_REQUIRED = "State is required";
    public static final String PINCODE_REQUIRED = "Pincode is required";
    public static final String PINCODE_INVALID = "Pincode must be 5 or 6 digits";
    public static final String COUNTRY_REQUIRED = "Country is required";
    public static final String ADDRESS_REQUIRED = "Address is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String ACCOUNT_VERIFICATION_FAILED = "Account Verification Failed";
    public static final String ACCOUNT_VERFIED = "Account Verification is already done";
    public static final String USERS_NOT_FOUND_BY_NAME = "Users not found with name";
    public static final String USERS_NOT_FOUND_BY_CITY = "Users not found with City";
    public static final String USERS_NOT_FOUND_BY_COUNTRY = "Users not found with County";
    public static final String USERS_NOT_FOUND_BY_EMAIL = "Users not found with Email";
    public static final String USERS_NOT_FOUND_BY_PINCODE = "Users not found with PINCODE";
    public static final String USERS_NOT_FOUND_BY_STATE = "Users not found with State";
    public static final String USERS_NOT_FOUND_BY_USER_ID= "Users not found with UserID";
    public static final String USERS_FOUND_BY_USER_ID= "There is an Existing account with this UserID.";
}