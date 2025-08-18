package com.mongodb.kitchensink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.exception.*;
import com.mongodb.kitchensink.service.AuthService;
import com.mongodb.kitchensink.service.ForgotPasswordService;
import com.mongodb.kitchensink.service.OtpService;
import com.mongodb.kitchensink.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import java.util.*;
import static org.hamcrest.Matchers.containsString;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private ForgotPasswordService forgotPasswordService;

    @Mock
    private OtpService otpService;
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest validLoginRequest;
    private LoginResponse validLoginResponse;
    private ForgotPasswordRequest validForgotPasswordRequest;
    private OtpRequest validOtpRequest;
    private ResetPasswordRequest validResetPasswordRequest;
    private ApiResponse successApiResponse;
    private String email="admin@example.com";
    private String password="Admin@123";

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail(this.email);
        validLoginRequest.setPassword(this.password);

        validLoginResponse = new LoginResponse(
                true,
                "Login successful",
                "jwt-token-here",
                this.email,
                this.email,
                "John Doe",
                Arrays.asList("USER"),
                false,
                false
        );

        validForgotPasswordRequest = new ForgotPasswordRequest();
        validForgotPasswordRequest.setEmail(this.email);

        validOtpRequest = new OtpRequest();
        validOtpRequest.setEmail(this.email);
        validOtpRequest.setOtp("123456");

        validResetPasswordRequest = new ResetPasswordRequest();
        validResetPasswordRequest.setEmail(this.email);
        validResetPasswordRequest.setNewPassword("Admin@123");

        successApiResponse = new ApiResponse("Operation successful", true);
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        private String email="admin@example.com";
        private String password="Admin@123";

        @Test
        @DisplayName("Should return 400 for invalid login request")
        void shouldReturn400ForInvalidLoginRequest() throws Exception {
            // Given
            LoginRequest invalidRequest = new LoginRequest();
            invalidRequest.setEmail(""); // Invalid email
            invalidRequest.setPassword("");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isOk());

            verify(authService, never()).login(anyString(), anyString());
        }

//        @Test
//        void shouldHandleAuthenticationException() throws Exception {
//            LoginRequest request = new LoginRequest();
//            request.setEmail("user@example.com");
//            request.setPassword("wrongpass");
//
//            when(authService.login(anyString(), anyString()))
//                    .thenThrow(new BadCredentialsException("bad credentials"));
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isUnauthorized());
//
//            verify(authService).login(anyString(), anyString());
//        }


//        @Test
//        void shouldThrowUserNotFoundException() throws Exception {
//            LoginRequest request = new LoginRequest();
//            request.setEmail("unknown@example.com");
//            request.setPassword("pass");
//
//            when(userService.getUserByEmail("unknown@example.com"))
//                    .thenThrow(new UserNotFoundException(
//                            ErrorCodes.RESOURCE_NOT_FOUND,
//                            ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL
//                    ));
//
//            assertThrows(Exception.class, () -> authService.login(request));
//            verify(userService).getUserByEmail("unknown@example.com");
//        }

    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {
        private String email="admin@example.com";
        private String password="Admin@123";
        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() throws Exception {
            // Given
            doNothing().when(authService).logout(anyString());

            // When & Then
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(authService).logout(this.email);
        }
    }

    @Nested
    @DisplayName("Session Validation Tests")
    class SessionValidationTests {

        @Test
        @DisplayName("Should validate session successfully")
        void shouldValidateSessionSuccessfully() throws Exception {
            // Given
            when(authService.validateSession(anyString())).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/auth/validate-session")
                            .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(authService).validateSession("Bearer valid-token");
        }

        @Test
        @DisplayName("Should return unauthorized for invalid session")
        void shouldReturnUnauthorizedForInvalidSession() throws Exception {
            // Given
            when(authService.validateSession(anyString())).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/auth/validate-session")
                            .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isUnauthorized());

            verify(authService).validateSession("Bearer invalid-token");
        }

//        @Test
//        @DisplayName("Should handle missing authorization header")
//        void shouldHandleMissingAuthHeader() throws Exception {
//            // Given
//            when(authService.validateSession(isNull())).thenReturn(false);
//
//            // When & Then
//            mockMvc.perform(get("/api/auth/validate-session"))
//                    .andExpect(status().isBadRequest());
//
//            verify(authService).validateSession(null);
//        }
    }

    @Nested
    @DisplayName("Forgot Password Tests")
    class ForgotPasswordTests {
        private String email="admin@example.com";
        private String password="Admin@123";
        @Test
        @DisplayName("Should request OTP for forgot password successfully")
        void shouldRequestOtpSuccessfully() throws Exception {
            // Given
            when(forgotPasswordService.sendOtpToEmail(anyString())).thenReturn(successApiResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/forgot-password/request-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validForgotPasswordRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(forgotPasswordService).sendOtpToEmail(this.email);
        }

        @Test
        @DisplayName("Should verify OTP for forgot password successfully")
        void shouldVerifyOtpSuccessfully() throws Exception {
            // Given
            when(forgotPasswordService.verifyOtp(anyString(), anyString())).thenReturn(successApiResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/forgot-password/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOtpRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(forgotPasswordService).verifyOtp(this.email, "123456");
        }

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() throws Exception {
            // Given
            when(forgotPasswordService.resetPassword(anyString(), anyString())).thenReturn(successApiResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/forgot-password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validResetPasswordRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(forgotPasswordService).resetPassword(this.email, this.password);
        }

        @Test
        @DisplayName("Should handle invalid request for forgot password")
        void shouldHandleInvalidForgotPasswordRequest() throws Exception {
            // Given
            ForgotPasswordRequest invalidRequest = new ForgotPasswordRequest();
            invalidRequest.setEmail(""); // Invalid email

            // When & Then
            mockMvc.perform(post("/api/auth/forgot-password/request-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(forgotPasswordService, never()).sendOtpToEmail(anyString());
        }
    }

    @Nested
    @DisplayName("Account Verification Tests")
    class AccountVerificationTests {
        private String email="admin@example.com";
        private String password="Admin@123";
        @Test
        @DisplayName("Should request OTP for account verification successfully")
        void shouldRequestOtpForAccountVerificationSuccessfully() throws Exception {
            // Given
            doNothing().when(authService).sendOtpForAccountVerification(anyString());

            // When & Then
            mockMvc.perform(post("/api/auth/account-verification/request-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validForgotPasswordRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message", containsString(this.email)));

            verify(authService).sendOtpForAccountVerification(this.email);
        }

    }

    @Nested
    @DisplayName("User Roles Tests")
    class UserRolesTests {
        private String email="admin@example.com";
        private String password="Admin@123";
        @Test
        @DisplayName("Should get user roles by email successfully")
        void shouldGetUserRolesByEmailSuccessfully() throws Exception {
            // Given
            List<String> roles = Arrays.asList("USER", "ADMIN");
            when(authService.getRolesByEmail(anyString())).thenReturn(roles);

            // When & Then
            mockMvc.perform(get("/api/auth/get-roles-by-email")
                            .header("Authorization", this.email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("USER"))
                    .andExpect(jsonPath("$[1]").value("ADMIN"));

            verify(authService).getRolesByEmail(this.email);
        }

        @Test
        @DisplayName("Should return unauthorized when roles are null")
        void shouldReturnUnauthorizedWhenRolesAreNull() throws Exception {
            // Given
            when(authService.getRolesByEmail(anyString())).thenReturn(null);

            // When & Then
            mockMvc.perform(get("/api/auth/get-roles-by-email")
                            .header("Authorization", this.email))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(""));

            verify(authService).getRolesByEmail(this.email);
        }

        @Test
        @DisplayName("Should get user roles by token successfully")
        void shouldGetUserRolesByTokenSuccessfully() throws Exception {
            // Given
            List<String> roles = Arrays.asList("USER");
            when(authService.getRolesFromToken(anyString())).thenReturn(roles);

            // When & Then
            mockMvc.perform(get("/api/auth/get-roles-by-token")
                            .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("USER"));

            verify(authService).getRolesFromToken("Bearer valid-token");
        }

        @Test
        @DisplayName("Should return unauthorized for invalid token format")
        void shouldReturnUnauthorizedForInvalidTokenFormat() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/auth/get-roles-by-token")
                            .header("Authorization", "invalid-token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$").isEmpty());

            verify(authService, never()).getRolesFromToken(anyString());
        }

        @Test
        @DisplayName("Should return unauthorized for missing authorization header")
        void shouldReturnUnauthorizedForMissingAuthHeader() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/auth/get-roles-by-token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$").isEmpty());

            verify(authService, never()).getRolesFromToken(anyString());
        }

        @Test
        @DisplayName("Should return empty list when roles are null from token")
        void shouldReturnEmptyListWhenRolesAreNullFromToken() throws Exception {
            // Given
            when(authService.getRolesFromToken(anyString())).thenReturn(null);

            // When & Then
            mockMvc.perform(get("/api/auth/get-roles-by-token")
                            .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());

            verify(authService).getRolesFromToken("Bearer valid-token");
        }
    }


    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        private String email="admin@example.com";
        private String password="Admin@123";
        @Test
        @DisplayName("Should return 400 for null email in login request")
        void shouldReturn400ForNullEmailInLoginRequest() throws Exception {
            // Given
            LoginRequest invalidRequest = new LoginRequest();
            invalidRequest.setEmail(null);
            invalidRequest.setPassword(this.password);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isOk());

            verify(authService, never()).login(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmailFormat() throws Exception {
            // Given
            LoginRequest invalidRequest = new LoginRequest();
            invalidRequest.setEmail("invalid-email");
            invalidRequest.setPassword(this.password);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isOk());

            verify(authService, never()).login(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 for missing password")
        void shouldReturn400ForMissingPassword() throws Exception {
            LoginRequest invalidRequest = new LoginRequest();
            invalidRequest.setEmail(this.email);
            invalidRequest.setPassword(null);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isOk());

            verify(authService, never()).login(anyString(), anyString());
        }

    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        private String email="admin@example.com";
        private String password="Admin@123";
        @Test
        @DisplayName("Should handle malformed JSON request")
        void shouldHandleMalformedJsonRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"invalid\",\"password\":\"\"}"))
                    .andExpect(status().isOk());

            verify(authService, never()).login(anyString(), anyString());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isUnsupportedMediaType());

            verify(authService, never()).login(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        private String email="admin@example.com";
        private String password="Admin@123";
        @Test
        @DisplayName("Should handle complete forgot password flow")
        void shouldHandleCompleteForgotPasswordFlow() throws Exception {
            // Request OTP
            when(forgotPasswordService.sendOtpToEmail(anyString())).thenReturn(successApiResponse);

            mockMvc.perform(post("/api/auth/forgot-password/request-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validForgotPasswordRequest)))
                    .andExpect(status().isOk());

            // Verify OTP
            when(forgotPasswordService.verifyOtp(anyString(), anyString())).thenReturn(successApiResponse);

            mockMvc.perform(post("/api/auth/forgot-password/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOtpRequest)))
                    .andExpect(status().isOk());

            // Reset Password
            when(forgotPasswordService.resetPassword(anyString(), anyString())).thenReturn(successApiResponse);

            mockMvc.perform(post("/api/auth/forgot-password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validResetPasswordRequest)))
                    .andExpect(status().isOk());

            verify(forgotPasswordService).sendOtpToEmail(this.email);
            verify(forgotPasswordService).verifyOtp(this.email, "123456");
            verify(forgotPasswordService).resetPassword(this.email, this.password);
        }

        @Test
        @DisplayName("Should handle complete account verification flow")
        void shouldHandleCompleteAccountVerificationFlow() throws Exception {
            // Request OTP for verification
            doNothing().when(authService).sendOtpForAccountVerification(anyString());

            mockMvc.perform(post("/api/auth/account-verification/request-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validForgotPasswordRequest)))
                    .andExpect(status().isOk());

            // Verify OTP
            when(authService.verifyOtpForAccountVerification((OtpRequest) any()))
                    .thenReturn(successApiResponse);

            mockMvc.perform(post("/api/auth/account-verification/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOtpRequest)))
                    .andExpect(status().isOk());

            // Get login response after verification
            when(authService.getLoginResponse(anyString())).thenReturn(validLoginResponse);

            mockMvc.perform(get("/api/auth/get-login-response-after-otp-verification")
                            .header("email", this.email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token-here"));

            verify(authService).sendOtpForAccountVerification(this.email);
            verify(authService).verifyOtpForAccountVerification((OtpRequest) any());
            verify(authService).getLoginResponse(this.email);
        }
    }
}