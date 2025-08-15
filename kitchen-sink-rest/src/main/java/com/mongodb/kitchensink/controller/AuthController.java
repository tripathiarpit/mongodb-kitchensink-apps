package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.service.AuthService;
import com.mongodb.kitchensink.service.ForgotPasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mongodb.kitchensink.constants.ErrorMessageConstants.INVALID_OR_EXPIRED_SESSION;
import static com.mongodb.kitchensink.constants.SuccessMessageConstants.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthService authService;

    private final ForgotPasswordService forgotPasswordService;

    public AuthController(AuthService authService, ForgotPasswordService forgotPasswordService) {
        this.authService = authService;
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/validate-session")
    public ResponseEntity<?> validateSession(@RequestHeader("Authorization") String authHeader) {
        boolean valid = authService.validateSession(authHeader);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_OR_EXPIRED_SESSION);
        }
        return ResponseEntity.ok(valid);
    }

    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<ApiResponse> requestOtp(@RequestBody ForgotPasswordRequest request) {
        ApiResponse response = forgotPasswordService.sendOtpToEmail(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }


    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        ApiResponse response =  forgotPasswordService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }


    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        ApiResponse response =  forgotPasswordService.resetPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
    @GetMapping("/get-roles-by-email")
    public ResponseEntity<List<String>> getUserRolesByEmail(@RequestHeader("Authorization") String email) {
        List<String> roles = authService.getRolesByEmail(email);
        if (roles== null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity.ok(roles);
    }
    @GetMapping("/get-roles-by-token")
    public ResponseEntity<List<String>> getUserRolesByToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
            List<String> roles = authService.getRolesFromToken(authHeader);

            if (roles == null) {
                roles = Collections.emptyList();
            }

            return ResponseEntity.ok(roles);

        } catch (Exception e) {
            e.printStackTrace(); // log the exact error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
