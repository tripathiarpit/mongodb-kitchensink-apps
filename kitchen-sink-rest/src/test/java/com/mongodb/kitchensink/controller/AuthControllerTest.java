package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.service.AuthService;
import com.mongodb.kitchensink.service.ForgotPasswordService;
import com.mongodb.kitchensink.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private ForgotPasswordService forgotPasswordService;
    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authService, forgotPasswordService, otpService);
    }

    @Test
    void login_shouldReturnLoginResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        LoginResponse response = new LoginResponse(
                true, // success
                "Login successful", // message
                "tokenValue", // accessToken
                "refreshTokenValue", // refreshToken
                "test@example.com", // email
                "testuser", // username
                "Test User", // fullName
                Arrays.asList("USER"), // roles
                false, // accountVerificationPending
                false // firstLogin
        );
        doNothing().when(authService).validateLoginRequest(request);
        when(authService.login(request)).thenReturn(response);

        ResponseEntity<LoginResponse> result = authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(authService).validateLoginRequest(request);
        verify(authService).login(request);
    }

    @Test
    void logout_shouldReturnApiResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        doNothing().when(authService).logout("test@example.com");

        ResponseEntity<ApiResponse> result = authController.logout(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        verify(authService).logout("test@example.com");
    }

    @Test
    void validateSession_shouldReturnOkIfValid() {
        when(authService.validateSession("token")).thenReturn(true);

        ResponseEntity<?> result = authController.validateSession("token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody());
    }

    @Test
    void validateSession_shouldReturnUnauthorizedIfInvalid() {
        when(authService.validateSession("token")).thenReturn(false);

        ResponseEntity<?> result = authController.validateSession("token");

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void refreshTokens_shouldReturnJwtAuthenticationResponse() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh");
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(
                "accessTokenValue",
                "refreshTokenValue"
        );
        when(authService.refreshTokens("refresh")).thenReturn(response);

        ResponseEntity<?> result = authController.refreshTokens(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void requestOtp_shouldReturnApiResponse() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        ApiResponse apiResponse = new ApiResponse("msg", true);
        when(forgotPasswordService.sendOtpToEmail("test@example.com")).thenReturn(apiResponse);

        ResponseEntity<ApiResponse> result = authController.requestOtp(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(apiResponse, result.getBody());
    }

    @Test
    void requestOtpForAccountVerification_shouldReturnApiResponse() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        doNothing().when(authService).sendOtpForAccountVerification("test@example.com");

        ResponseEntity<ApiResponse> result = authController.requestOtpForAccountVerification(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        verify(authService).sendOtpForAccountVerification("test@example.com");
    }

    @Test
    void verify_shouldReturnApiResponse() throws Exception {
        OtpRequest request = new OtpRequest();
        ApiResponse apiResponse = new ApiResponse("msg", true);
        when(authService.verifyOtpForAccountVerification(request)).thenReturn(apiResponse);

        ResponseEntity<ApiResponse> result = authController.verify(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(apiResponse, result.getBody());
    }

    @Test
    void verifyOtp_shouldReturnApiResponse() {
        OtpRequest request = new OtpRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        ApiResponse apiResponse = new ApiResponse("msg", true);
        when(forgotPasswordService.verifyOtp("test@example.com", "123456")).thenReturn(apiResponse);

        ResponseEntity<ApiResponse> result = authController.verifyOtp(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(apiResponse, result.getBody());
    }

    @Test
    void resetPassword_shouldReturnApiResponse() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setNewPassword("pass");
        ApiResponse apiResponse = new ApiResponse("msg", true);
        when(forgotPasswordService.resetPassword("test@example.com", "pass")).thenReturn(apiResponse);

        ResponseEntity<ApiResponse> result = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(apiResponse, result.getBody());
    }

    @Test
    void getUserRolesByEmail_shouldReturnRoles() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        when(authService.getRolesByEmail("test@example.com")).thenReturn(roles);

        ResponseEntity<List<String>> result = authController.getUserRolesByEmail("test@example.com");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(roles, result.getBody());
    }

    @Test
    void getUserRolesByEmail_shouldReturnUnauthorizedIfNull() {
        when(authService.getRolesByEmail("test@example.com")).thenReturn(null);

        ResponseEntity<List<String>> result = authController.getUserRolesByEmail("test@example.com");

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getUserRolesByToken_shouldReturnRoles() {
        List<String> roles = Collections.singletonList("USER");
        when(authService.getRolesFromToken("token")).thenReturn(roles);

        ResponseEntity<List<String>> result = authController.getUserRolesByToken("token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(roles, result.getBody());
    }

    @Test
    void getUserRolesByToken_shouldReturnUnauthorizedOnException() {
        when(authService.getRolesFromToken("token")).thenThrow(new RuntimeException());

        ResponseEntity<List<String>> result = authController.getUserRolesByToken("token");

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals(Collections.emptyList(), result.getBody());
    }

    @Test
    void getLoginResponse_shouldReturnLoginResponse() {
        LoginResponse response = new LoginResponse(
                true, // success
                "Login successful", // message
                "tokenValue", // accessToken
                "refreshTokenValue", // refreshToken
                "test@example.com", // email
                "testuser", // username
                "Test User", // fullName
                Arrays.asList("USER"), // roles
                false, // accountVerificationPending
                false // firstLogin
        );
        when(authService.getLoginResponse("test@example.com")).thenReturn(response);

        ResponseEntity<LoginResponse> result = authController.getLoginResponse("test@example.com");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}