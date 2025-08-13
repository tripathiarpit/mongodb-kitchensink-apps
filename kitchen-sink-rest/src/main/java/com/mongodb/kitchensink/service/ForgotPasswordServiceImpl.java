package com.mongodb.kitchensink.service;


import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.dto.ApiResponse;
import com.mongodb.kitchensink.exception.InvalidOtpException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mongodb.kitchensink.constants.SuccessMessageConstants.*;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService   {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordServiceImpl(UserRepository userRepository,
                                     OtpService otpService,
                                     EmailService emailService,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ApiResponse sendOtpToEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ApiResponse.error(
                    ErrorCodes.RESOURCE_NOT_FOUND,
                    ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL
            );
        }

        User user = optionalUser.get();

        String otp = otpService.generateOtp(email);

        emailService.sendEmail(
                user.getEmail(),
                PASSWORD_RESET_OTP_SUBJECT,
                String.format(PASSWORD_RESET_OTP_BODY_TEMPLATE, otp) // Insert OTP into template
        );

         return new ApiResponse(OTP_SENT_SUCCESS+ " : "+ email+ ".", true);
    }

    @Override
    public ApiResponse verifyOtp(String email, String otp) {
        boolean valid = otpService.validateOtp(email, otp);
        if (!valid) {
            throw new InvalidOtpException(ErrorCodes.VALIDATION_ERROR, ErrorMessageConstants.INVALID_OR_EXPIRED_OTP);
        }
        return new ApiResponse(OTP_VERIFIED_SUCCESS, true);
    }

    @Override
    public ApiResponse resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpService.clearOtp(email);

        return new ApiResponse(PASSWORD_RESET_SUCCESS, true);
    }
}
