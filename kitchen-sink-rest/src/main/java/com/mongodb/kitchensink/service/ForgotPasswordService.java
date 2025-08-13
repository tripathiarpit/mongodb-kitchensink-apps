package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.ApiResponse;

public interface ForgotPasswordService {
    ApiResponse sendOtpToEmail(String email);
    ApiResponse verifyOtp(String email, String otp);
    ApiResponse resetPassword(String email, String newPassword);
}
