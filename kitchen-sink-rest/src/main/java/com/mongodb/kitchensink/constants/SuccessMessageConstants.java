package com.mongodb.kitchensink.constants;

public class SuccessMessageConstants {
    private SuccessMessageConstants() {

    }

    public static final String SESSION_VALID = "Session valid";
    public static final String OTP_SENT_SUCCESS = "OTP sent successfully to";
    public static final String OTP_VERIFIED_SUCCESS = "OTP verified successfully";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";
    public static final String ACCOUNT_CREATED_SUCCESSFULLY = "Account created successfully and it's pending for verification.Please check your email.";
    public static final String ACCOUNT_DELETED_SUCCESSFULLY = "Account has been deleted successfully";
    public static final String LOGGED_OUT_SUCCESSFULLY = "Logged out successfully";
    public static final String LOGGED_IN_SUCCESSFULLY = "Logged in successfully";

    public static final String PASSWORD_RESET_OTP_SUBJECT =
            "MongoDB Kitchensink - Password Reset OTP";

    public static final String PASSWORD_RESET_OTP_BODY_TEMPLATE =
            "<html>" +
                    "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
                    + "<p>Hello <b>%s</b>,</p>"
                    + "<p>We received a request to reset your password.</p>"
                    + "<p>Your OTP is:</p>"
                    + "<div style='font-size: 24px; font-weight: bold; color: white; "
                    + "background-color: #4CAF50; padding: 10px 20px; border-radius: 8px; "
                    + "display: inline-block;'>%s</div>"
                    + "<p>This code will expire in <b>5 minutes</b>.</p>"
                    + "<p>If you did not request a password reset, please ignore this message.</p>"
                    + "<br/>"
                    + "<p>Thank you,<br/>MongoDB Kitchensink Team</p>"
                    + "</body></html>";

    public static final String ACCOUNT_VERIFICATION_SUBJECT =
            "MongoDB Kitchensink - Account Created Successfully";

    public static final String ACCOUNT_VERIFICATION_BODY_TEMPLATE =
            "<html>" +
                    "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
                    + "<p>Hello <b>%s</b>,</p>"
                    + "<p><i>Congratulations!</i> Your account has been created successfully.</p>"
                    + "<p>Your OTP for account verification is:</p>"
                    + "<div style='font-size: 24px; font-weight: bold; color: white; "
                    + "background-color: #2196F3; padding: 10px 20px; border-radius: 8px; "
                    + "display: inline-block;'>%s</div>"
                    + "<p>This code will expire in <b>2 hours</b>.</p>"
                    + "<p>Please enter this code in the app to verify your account.</p>"
                    + "<br/>"
                    + "<p>Welcome aboard,<br/>MongoDB Kitchensink Team</p>"
                    + "</body></html>";
}
