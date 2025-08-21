package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.constants.SuccessMessageConstants;
import com.mongodb.kitchensink.dto.ApiResponse;
import com.mongodb.kitchensink.exception.InvalidOtpException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForgotPasswordServiceImpl Tests")
class ForgotPasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ForgotPasswordServiceImpl forgotPasswordService;

    private final String EMAIL = "test@example.com";
    private final String OTP = "123456";
    private final String NEW_PASSWORD = "newPassword123";
    private final String ENCODED_PASSWORD = "encodedPasswordHash";
    private final String USERNAME = "testuser";
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail(EMAIL);
        mockUser.setUsername(USERNAME);
        ReflectionTestUtils.setField(forgotPasswordService, "forgotPasswordTtl", 900L);
    }

    // --- sendOtpToEmail Tests ---
    @Test
    @DisplayName("should send OTP and return success for existing user")
    void sendOtpToEmail_existingUser_shouldSendOtpAndReturnSuccess() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(otpService.generateOtp(eq(EMAIL), eq("FORGOT_PASSWORD"), anyLong())).thenReturn(OTP);

        // When
        ApiResponse response = forgotPasswordService.sendOtpToEmail(EMAIL);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(SuccessMessageConstants.OTP_SENT_SUCCESS + " : " + EMAIL + ".", response.getMessage());
        verify(otpService, times(1)).generateOtp(eq(EMAIL), eq("FORGOT_PASSWORD"), anyLong());
        verify(emailService, times(1)).sendEmail(
                eq(mockUser.getEmail()),
                eq(SuccessMessageConstants.PASSWORD_RESET_OTP_SUBJECT),
                anyString()
        );
    }

    @Test
    @DisplayName("should return error for non-existing user")
    void sendOtpToEmail_nonExistingUser_shouldReturnError() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When
        ApiResponse response = forgotPasswordService.sendOtpToEmail(EMAIL);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL, response.getMessage());
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, response.getCode());
        verify(otpService, never()).generateOtp(anyString(), anyString(), anyLong());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // --- verifyOtp Tests ---
    @Test
    @DisplayName("should return success for valid OTP")
    void verifyOtp_validOtp_shouldReturnSuccess() {
        // Given
        when(otpService.verifyOtp(EMAIL, "FORGOT_PASSWORD", OTP)).thenReturn(true);

        // When
        ApiResponse response = forgotPasswordService.verifyOtp(EMAIL, OTP);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(SuccessMessageConstants.OTP_VERIFIED_SUCCESS, response.getMessage());
        verify(otpService, times(1)).verifyOtp(EMAIL, "FORGOT_PASSWORD", OTP);
    }

    @Test
    @DisplayName("should throw InvalidOtpException for invalid OTP")
    void verifyOtp_invalidOtp_shouldThrowException() {
        // Given
        when(otpService.verifyOtp(EMAIL, "FORGOT_PASSWORD", OTP)).thenReturn(false);

        // When & Then
        InvalidOtpException exception = assertThrows(InvalidOtpException.class, () -> {
            forgotPasswordService.verifyOtp(EMAIL, OTP);
        });

        assertEquals(ErrorCodes.VALIDATION_ERROR, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.INVALID_OR_EXPIRED_OTP, exception.getMessage());
        verify(otpService, times(1)).verifyOtp(EMAIL, "FORGOT_PASSWORD", OTP);
    }

    // --- resetPassword Tests ---
    @Test
    @DisplayName("should reset password for valid user")
    void resetPassword_validUser_shouldResetPassword() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // When
        ApiResponse response = forgotPasswordService.resetPassword(EMAIL, NEW_PASSWORD);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(SuccessMessageConstants.PASSWORD_RESET_SUCCESS, response.getMessage());
        assertEquals(ENCODED_PASSWORD, mockUser.getPasswordHash());
        verify(userRepository, times(1)).save(mockUser);
        verify(otpService, times(1)).clearOtp(EMAIL, "FORGOT_PASSWORD");
    }

    @Test
    @DisplayName("should throw UserNotFoundException for non-existing user")
    void resetPassword_nonExistingUser_shouldThrowException() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            forgotPasswordService.resetPassword(EMAIL, NEW_PASSWORD);
        });

        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL, exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(otpService, never()).clearOtp(anyString(), anyString());
    }
}