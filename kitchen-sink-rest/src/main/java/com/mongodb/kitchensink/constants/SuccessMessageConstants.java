package com.mongodb.kitchensink.constants;

public class SuccessMessageConstants {
    private SuccessMessageConstants() {

    }

    public static final String SESSION_VALID = "Session valid";
    public static final String OTP_SENT_SUCCESS = "OTP sent successfully to";
    public static final String OTP_VERIFIED_SUCCESS = "OTP verified successfully";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";


    public static final String PASSWORD_RESET_OTP_SUBJECT = "MongoDB kitchensink, Password Reset OTP";
    public static final String PASSWORD_RESET_OTP_BODY_TEMPLATE =
            "Your OTP for password reset is: %s\nIt will expire in 5 minutes.";
}
