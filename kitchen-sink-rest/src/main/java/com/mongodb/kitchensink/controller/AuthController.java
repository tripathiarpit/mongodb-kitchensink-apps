package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.service.AuthService;
import com.mongodb.kitchensink.service.ForgotPasswordService;
import com.mongodb.kitchensink.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

import static com.mongodb.kitchensink.constants.ErrorMessageConstants.*;
import static com.mongodb.kitchensink.constants.SuccessMessageConstants.*;

/**
 * Controller for authentication and authorization operations.
 * <p>
 * Provides endpoints for login, logout, session validation, password recovery,
 * account verification via OTP, and retrieval of user roles.
 * </p>
 *
 * @author Arpit Tripathi
 * @version 1.0
 * @since 2025-08-17
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for login, logout, OTP verification, and password management")
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    public AuthController(AuthService authService, ForgotPasswordService forgotPasswordService, OtpService otpService) {
        this.authService = authService;
        this.forgotPasswordService = forgotPasswordService;
    }

    @Operation(summary = "Login user", description = "Authenticates user with email and password and returns login response including token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) throws Exception {
        authService.validateLoginRequest(loginRequest);
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(summary = "Logout user", description = "Logs out the user by invalidating their session")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody @Valid LoginRequest loginRequest) throws Exception {
        authService.logout(loginRequest.getEmail());
        return ResponseEntity.ok(new ApiResponse(LOGGED_OUT_SUCCESSFULLY, true));
    }

    @Operation(summary = "Validate session token", description = "Validates the session token from Authorization header")
    @GetMapping("/validate-session")
    public ResponseEntity<?> validateSession(@RequestHeader("Authorization") String authHeader) {
        boolean valid = authService.validateSession(authHeader);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_OR_EXPIRED_SESSION);
        }
        return ResponseEntity.ok(valid);
    }

    @Operation(summary = "Request OTP for forgot password", description = "Sends an OTP to the user's email for password reset")
    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<ApiResponse> requestOtp(@RequestBody @Valid ForgotPasswordRequest request) {
        return ResponseEntity.ok(forgotPasswordService.sendOtpToEmail(request.getEmail()));
    }

    @Operation(summary = "Request OTP for account verification", description = "Sends an OTP to the user's email for account verification")
    @PostMapping("/account-verification/request-otp")
    public ResponseEntity<ApiResponse> requestOtpForAccountVerification(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) throws Exception {
        authService.sendOtpForAccountVerification(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok(new ApiResponse(OTP_SENT_SUCCESS + " : " + forgotPasswordRequest.getEmail() + ".", true));
    }

    @Operation(summary = "Verify OTP for account verification", description = "Verifies the OTP sent for account verification")
    @PostMapping("/account-verification/verify-otp")
    public ResponseEntity<ApiResponse> verify(@RequestBody @Valid OtpRequest request) throws Exception {
        return ResponseEntity.ok(authService.verifyOtpForAccountVerification(request));
    }

    @Operation(summary = "Verify OTP for forgot password", description = "Verifies the OTP sent for password recovery")
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody @Valid OtpRequest request) {
        return ResponseEntity.ok(forgotPasswordService.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @Operation(summary = "Reset password", description = "Resets the user's password after OTP verification")
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(forgotPasswordService.resetPassword(request.getEmail(), request.getNewPassword()));
    }

    @Operation(summary = "Get user roles by email", description = "Returns roles of the user based on the provided email")
    @GetMapping("/get-roles-by-email")
    public ResponseEntity<List<String>> getUserRolesByEmail(@RequestHeader("Authorization") String email) {
        List<String> roles = authService.getRolesByEmail(email);
        if (roles == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Get user roles by token", description = "Extracts user roles from the JWT token provided in the Authorization header")
    @GetMapping("/get-roles-by-token")
    public ResponseEntity<List<String>> getUserRolesByToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
            List<String> roles = authService.getRolesFromToken(authHeader);
            return ResponseEntity.ok(roles != null ? roles : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }
    }

    @Operation(summary = "Get login response after OTP verification", description = "Returns login response for a user after successful OTP verification")
    @GetMapping("/get-login-response-after-otp-verification")
    public ResponseEntity<LoginResponse> getLoginResponse(@RequestHeader("email") String email) {
        return ResponseEntity.ok(authService.getLoginResponse(email));
    }
}
