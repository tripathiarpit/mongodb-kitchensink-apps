package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.exception.*;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static com.mongodb.kitchensink.constants.AppContants.ACCOUNT_VERIFICATION;
import static com.mongodb.kitchensink.constants.ErrorMessageConstants.*;
import static com.mongodb.kitchensink.constants.SuccessMessageConstants.OTP_VERIFIED_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @Mock
    private SessionService sessionService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private UserDto userDto;
    private UserDetails userDetails;
    private Authentication authentication;
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String TOKEN = "mock-jwt-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accountVerificationTtl", 300L);
        ReflectionTestUtils.setField(authService, "forgotPasswordTtl", 300L);

        loginRequest = new LoginRequest(EMAIL, PASSWORD);

        ProfileDto profileDto = new ProfileDto();
        profileDto.setFirstName("Test");
        profileDto.setLastName("User");
        userDto = new UserDto();
        userDto.setEmail(EMAIL);
        userDto.setUsername("testuser");
        userDto.setProfile(profileDto);
        userDto.setAccountVerificationPending(false);
        userDto.setActive(true);
        userDto.setFirstLogin(false);
        userDto.setRoles(Collections.singletonList("ROLE_USER"));

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(EMAIL)
                .password("hashedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(EMAIL);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication)
                .getAuthorities();

        lenient().when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        lenient().when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        lenient().when(sessionService.doesSessionExist(anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("should throw InvalidRequestException for null or empty password")
    void login_shouldThrowInvalidRequestExceptionForNullOrEmptyPassword() {
        LoginRequest nullPasswordRequest = new LoginRequest(EMAIL, null);
        LoginRequest emptyPasswordRequest = new LoginRequest(EMAIL, "");
        assertThrows(InvalidRequestException.class, () -> authService.login(nullPasswordRequest));
        assertThrows(InvalidRequestException.class, () -> authService.login(emptyPasswordRequest));
    }

    @Test
    @DisplayName("should throw UserAuthException if user is not active")
    void login_shouldThrowUserAuthExceptionIfUserNotActive() {
        userDto.setActive(false);
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        assertThrows(UserAuthException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("should map BadCredentialsException to UserAuthException in login")
    void login_shouldMapBadCredentialsException() {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));
        UserAuthException ex = assertThrows(UserAuthException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCodes.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw InvalidRequestException when authHeader is null")
    void getRolesFromToken_shouldThrowForNullHeader() {
        assertThrows(InvalidRequestException.class, () -> authService.getRolesFromToken(null));
    }

    @Test
    @DisplayName("should throw InvalidRequestException when authHeader does not start with Bearer")
    void getRolesFromToken_shouldThrowForInvalidPrefix() {
        String invalidHeader = "Token abc.def.ghi";
        assertThrows(InvalidRequestException.class, () -> authService.getRolesFromToken(invalidHeader));
    }

    @Test
    @DisplayName("should return roles for valid Bearer token")
    void getRolesFromToken_shouldReturnRolesForValidToken() {
        String authHeader = "Bearer " + TOKEN;
        when(jwtTokenProvider.getEmailFromAccessToken(anyString())).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        doNothing().when(jwtTokenProvider).validateAccessToken(anyString());
        when(jwtTokenProvider.getRolesFromToken(anyString())).thenReturn(List.of("ROLE_USER")); // <-- Add this line
        List<String> roles = authService.getRolesFromToken(authHeader);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    @DisplayName("should throw InvalidOtpException when request is null")
    void verifyOtpForAccountVerification_shouldThrowForNullRequest() {
        assertThrows(InvalidOtpException.class, () -> authService.verifyOtpForAccountVerification(null));
    }

    @Test
    @DisplayName("should throw InvalidOtpException when email is null")
    void verifyOtpForAccountVerification_shouldThrowForNullEmail() {
        OtpRequest req = new OtpRequest(null, "123456");
        assertThrows(InvalidOtpException.class, () -> authService.verifyOtpForAccountVerification(req));
    }

    @Test
    @DisplayName("should throw InvalidOtpException when email is empty")
    void verifyOtpForAccountVerification_shouldThrowForEmptyEmail() {
        OtpRequest req = new OtpRequest("", "123456");
        assertThrows(InvalidOtpException.class, () -> authService.verifyOtpForAccountVerification(req));
    }

    @Test
    @DisplayName("should throw InvalidOtpException when otp is null")
    void verifyOtpForAccountVerification_shouldThrowForNullOtp() {
        OtpRequest req = new OtpRequest("user@example.com", null);
        assertThrows(InvalidOtpException.class, () -> authService.verifyOtpForAccountVerification(req));
    }

    @Test
    @DisplayName("should throw InvalidOtpException when otp is empty")
    void verifyOtpForAccountVerification_shouldThrowForEmptyOtp() {
        OtpRequest req = new OtpRequest("user@example.com", "");
        assertThrows(InvalidOtpException.class, () -> authService.verifyOtpForAccountVerification(req));
    }

    @Test
    @DisplayName("should throw InvalidRequestException when authHeader is null")
    void validateSession_shouldThrowForNullHeader() {
        assertThrows(InvalidRequestException.class, () -> authService.validateSession(null));
    }

    @Test
    @DisplayName("should throw InvalidRequestException when authHeader does not start with Bearer")
    void validateSession_shouldThrowForInvalidPrefix() {
        String invalidHeader = "Token abc.def.ghi";
        assertThrows(InvalidRequestException.class, () -> authService.validateSession(invalidHeader));
    }

    @Test
    @DisplayName("should return false for valid session token but user does not exist")
    void validateSession_shouldReturnFalseForValidTokenButUserNotFound() {
        String authHeader = "Bearer " + TOKEN;
        when(sessionService.doesSessionExist(anyString())).thenReturn(true);
        when(jwtTokenProvider.getEmailFromAccessToken(anyString())).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(null);
        assertFalse(authService.validateSession(authHeader));
    }
    @Test
    @DisplayName("should propagate exception if jwtTokenProvider.getEmailFromAccessToken throws")
    void validateSession_shouldPropagateExceptionFromJwtTokenProvider() {
        String authHeader = "Bearer " + TOKEN;
        when(sessionService.doesSessionExist(anyString())).thenReturn(true);
        doThrow(new RuntimeException("Session invalid")).when(jwtTokenProvider).getEmailFromAccessToken(anyString());
        assertThrows(RuntimeException.class, () -> authService.validateSession(authHeader));
    }

    @Test
    @DisplayName("should propagate exception if userDetailsService.loadUserByUsername throws")
    void validateSession_shouldPropagateExceptionFromUserDetailsService() {
        String authHeader = "Bearer " + TOKEN;
        when(sessionService.doesSessionExist(anyString())).thenReturn(true);
        when(jwtTokenProvider.getEmailFromAccessToken(anyString())).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new RuntimeException("UserDetails error"));
        assertThrows(RuntimeException.class, () -> authService.validateSession(authHeader));
    }

    @Test
    @DisplayName("should activate account and return success for valid OTP")
    void verifyOtpForAccountVerification_shouldSucceedForValidOtp() throws Exception {
        OtpRequest req = new OtpRequest("user@example.com", "123456");
        when(otpService.verifyOtp(anyString(), anyString(), anyString())).thenReturn(true);
        ApiResponse response = authService.verifyOtpForAccountVerification(req);
        assertTrue(response.isSuccess());
        assertEquals(OTP_VERIFIED_SUCCESS, response.getMessage());
        verify(userService, times(1)).activateAccount(eq("user@example.com"), eq(true));
    }

    @Test
    @DisplayName("should throw InvalidRequestException when userDetails is null")
    void getRolesFromToken_shouldThrowWhenUserDetailsIsNull() {
        String authHeader = "Bearer " + TOKEN;
        when(jwtTokenProvider.getEmailFromAccessToken(anyString())).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(null);
        doNothing().when(jwtTokenProvider).validateAccessToken(anyString());
        assertThrows(InvalidRequestException.class, () -> authService.getRolesFromToken(null));
    }

    @Test
    @DisplayName("should throw BadRequestException when password is null")
    void validateLoginRequest_shouldThrowForNullPassword() {
        LoginRequest req = new LoginRequest("user@example.com", null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(req));
        assertEquals(REQ_PASSWORD, ex.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException when password is blank")
    void validateLoginRequest_shouldThrowForBlankPassword() {
        LoginRequest req = new LoginRequest("user@example.com", "   ");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(req));
        assertEquals(REQ_PASSWORD, ex.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException when email is null")
    void validateLoginRequest_shouldThrowForNullEmail() {
        LoginRequest req = new LoginRequest(null, "password");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(req));
        assertEquals(REQ_EMAIL, ex.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException when email is blank")
    void validateLoginRequest_shouldThrowForBlankEmail() {
        LoginRequest req = new LoginRequest("   ", "password");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(req));
        assertEquals(REQ_EMAIL, ex.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException for invalid email format")
    void validateLoginRequest_shouldThrowForInvalidEmailFormat() {
        LoginRequest req = new LoginRequest("invalid-email", "password");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(req));
        assertEquals(INVALID_EMAIL_FORMET, ex.getMessage());
    }

    @Test
    @DisplayName("should not throw for valid email and password")
    void validateLoginRequest_shouldPassForValidInput() {
        LoginRequest req = new LoginRequest("user@example.com", "password");
        assertDoesNotThrow(() -> authService.validateLoginRequest(req));
    }

    @Test
    @DisplayName("should map DisabledException to UserAuthException in login")
    void login_shouldMapDisabledException() {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.DisabledException("disabled"));
        UserAuthException ex = assertThrows(UserAuthException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCodes.ACCOUNT_DISABLED, ex.getErrorCode());
    }

    @Test
    @DisplayName("should rethrow UserNotFoundException in login")
    void login_shouldRethrowUserNotFoundException() {
        when(userService.getUserByEmail(anyString())).thenThrow(new UserNotFoundException(ErrorCodes.USER_NOT_FOUND, "not found"));
        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("should rethrow UserAuthException in login")
    void login_shouldRethrowUserAuthException() {
        when(userService.getUserByEmail(anyString())).thenThrow(new UserAuthException(ErrorCodes.ACCOUNT_DISABLED));
        assertThrows(UserAuthException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("should wrap unexpected Exception in RuntimeException in login")
    void login_shouldWrapUnexpectedException() {
        when(userService.getUserByEmail(anyString())).thenThrow(new RuntimeException("unexpected"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Unexpected error", ex.getMessage());
    }

    @Test
    @DisplayName("should return successful response for valid credentials")
    void login_shouldReturnSuccessfulResponseForValidCredentials() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(sessionService.doesSessionExist(anyString())).thenReturn(false);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyList())).thenReturn(TOKEN);
        LoginResponse response = authService.login(loginRequest);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(TOKEN, response.getAccessToken());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    @DisplayName("should handle first login and verification")
    void login_shouldHandleFirstLoginAndVerification() throws Exception {
        userDto.setAccountVerificationPending(true);
        userDto.setFirstLogin(true);
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setAccountVerificationPending(true);
        updatedUserDto.setFirstLogin(false);
        updatedUserDto.setProfile(userDto.getProfile());
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(userService.getUserByEmailForVerification(anyString())).thenReturn(new User());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(sessionService.doesSessionExist(anyString())).thenReturn(false);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyList())).thenReturn(TOKEN);
        LoginResponse response = authService.login(loginRequest);
        assertTrue(response.isSuccess());
        assertEquals("Logged in successfully", response.getMessage());
        assertNull(response.getAccessToken());
        assertTrue(response.getAccountVerificationPending());
        assertTrue(response.getFirstLogin());
        verify(userService, times(1)).saveUpdatedUserAfterVerification(any(User.class));
    }

    @Test
    @DisplayName("should throw UserAuthException for bad credentials")
    void login_shouldThrowUserAuthExceptionForBadCredentials() {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid password"));
        UserAuthException exception = assertThrows(UserAuthException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCodes.INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    @DisplayName("should throw BadRequestException for null email")
    void validateLoginRequest_shouldThrowExceptionForNullEmail() {
        LoginRequest invalidRequest = new LoginRequest(null, PASSWORD);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(invalidRequest));
        assertEquals(REQ_EMAIL, exception.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException for invalid email format")
    void validateLoginRequest_shouldThrowExceptionForInvalidEmailFormat() {
        LoginRequest invalidRequest = new LoginRequest("invalid-email", PASSWORD);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(invalidRequest));
        assertEquals(INVALID_EMAIL_FORMET, exception.getMessage());
    }

    @Test
    @DisplayName("should invalidate session token for a valid logout")
    void logout_shouldInvalidateSessionToken() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(sessionService.doesSessionExist(anyString())).thenReturn(true);
        authService.logout(EMAIL);
        verify(sessionService, times(1)).invalidateSession(EMAIL);
    }

    @Test
    @DisplayName("should not throw exception if session token does not exist")
    void logout_shouldNotThrowExceptionIfSessionDoesNotExist() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(sessionService.doesSessionExist(anyString())).thenReturn(false);
        assertDoesNotThrow(() -> authService.logout(EMAIL));
        verify(sessionService, never()).invalidateSession(anyString());
    }

    @Test
    @DisplayName("should return true for a valid session token")
    void validateSession_shouldReturnTrueForValidToken() {
        String authHeader = "Bearer " + TOKEN;
        doNothing().when(jwtTokenProvider).validateAccessToken(anyString());
        when(jwtTokenProvider.getEmailFromAccessToken(anyString())).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        boolean isValid = authService.validateSession(authHeader);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("should throw InvalidRequestException for invalid auth header format")
    void validateSession_shouldThrowExceptionForInvalidHeader() {
        String authHeader = "Invalid " + TOKEN;
        assertThrows(InvalidRequestException.class, () -> authService.validateSession(authHeader));
    }

    @Test
    @DisplayName("should return roles for valid email")
    void getRolesByEmail_shouldReturnRolesForValidEmail() {
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        List<String> roles = authService.getRolesByEmail(EMAIL);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    @DisplayName("should throw UserNotFoundException for non-existent user email")
    void getRolesByEmail_shouldThrowUserNotFoundException() {
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.getRolesByEmail(EMAIL));
        assertEquals(ErrorCodes.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("should generate and send OTP for account verification")
    void sendOtpForAccountVerification_shouldSucceed() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(otpService.generateOtp(anyString(), anyString(), anyLong())).thenReturn("123456");
        assertDoesNotThrow(() -> authService.sendOtpForAccountVerification(EMAIL));
        verify(otpService, times(1)).generateOtp(eq(EMAIL), eq(ACCOUNT_VERIFICATION), anyLong());
        verify(emailService, times(1)).sendEmail(eq(EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("should activate account successfully after OTP verification")
    void verifyOtpForAccountVerification_shouldSucceed() throws Exception {
        OtpRequest otpRequest = new OtpRequest(EMAIL, "123456");
        when(otpService.verifyOtp(anyString(), anyString(), anyString())).thenReturn(true);
        ApiResponse response = authService.verifyOtpForAccountVerification(otpRequest);
        assertTrue(response.isSuccess());
        assertEquals(OTP_VERIFIED_SUCCESS, response.getMessage());
        verify(userService, times(1)).activateAccount(eq(EMAIL), eq(true));
    }

    @Test
    @DisplayName("should throw InvalidOtpException for invalid OTP")
    void verifyOtpForAccountVerification_shouldThrowForInvalidOtp() {
        OtpRequest otpRequest = new OtpRequest(EMAIL, "invalid_otp");
        when(otpService.verifyOtp(anyString(), anyString(), anyString())).thenReturn(false);
        InvalidOtpException exception = assertThrows(InvalidOtpException.class, () -> authService.verifyOtpForAccountVerification(otpRequest));
        assertEquals(ErrorCodes.ACCOUNT_VERIFICATION_FAILED, exception.getErrorCode());
    }

    @Test
    @DisplayName("should return login response for a verified user")
    void getLoginResponse_shouldReturnResponseForVerifiedUser() {
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(sessionService.doesSessionExist(anyString())).thenReturn(true);
        when(sessionService.getTokenForExistingSession(anyString())).thenReturn(TOKEN);
        LoginResponse response = authService.getLoginResponse(EMAIL);
        assertTrue(response.isSuccess());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    @DisplayName("should throw AccountVerificationExcpetion for a pending user")
    void getLoginResponse_shouldThrowForPendingUser() {
        userDto.setAccountVerificationPending(true);
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        AccountVerificationException exception = assertThrows(AccountVerificationException.class, () -> authService.getLoginResponse(EMAIL));
        assertEquals(ErrorCodes.ACCOUNT_VERIFICATION_PENDING, exception.getErrorCode());
    }
    // Test: getRolesByEmail should throw for null/empty email
    @Test
    @DisplayName("should throw UserNotFoundException for null or empty email in getRolesByEmail")
    void getRolesByEmail_shouldThrowForNullOrEmptyEmail() {
        assertThrows(UserNotFoundException.class, () -> authService.getRolesByEmail(null));
        assertThrows(UserNotFoundException.class, () -> authService.getRolesByEmail(""));
    }

    // Test: isUserAdminOrUser returns true for admin/user roles, false otherwise
    @Test
    @DisplayName("should return true if roles contain ROLE_ADMIN or ROLE_USER")
    void isUserAdminOrUser_shouldReturnTrueForAdminOrUser() {
        assertTrue(authService.isUserAdminOrUser(List.of("ROLE_ADMIN")));
        assertTrue(authService.isUserAdminOrUser(List.of("ROLE_USER")));
        assertFalse(authService.isUserAdminOrUser(List.of("ROLE_GUEST")));
    }

    // Test: getRolesFromToken should throw for expired token
    @Test
    @DisplayName("should throw InvalidRequestException for expired token in getRolesFromToken")
    void getRolesFromToken_shouldThrowForExpiredToken() {
        String authHeader = "Bearer " + TOKEN;
        doThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired")).when(jwtTokenProvider).validateAccessToken(anyString());
        assertThrows(InvalidRequestException.class, () -> authService.getRolesFromToken(authHeader));
    }

    // Test: getRolesFromToken should throw for invalid token
    @Test
    @DisplayName("should throw InvalidRequestException for invalid token in getRolesFromToken")
    void getRolesFromToken_shouldThrowForInvalidToken() {
        String authHeader = "Bearer " + TOKEN;
        doThrow(new RuntimeException("invalid")).when(jwtTokenProvider).validateAccessToken(anyString());
        assertThrows(InvalidRequestException.class, () -> authService.getRolesFromToken(authHeader));
    }

    // Test: refreshTokens should throw InvalidRequestException for null refresh token
    @Test
    @DisplayName("should throw InvalidRequestException for null refresh token in refreshTokens")
    void refreshTokens_shouldThrowForNullRefreshToken() {
        assertThrows(InvalidRequestException.class, () -> authService.refreshTokens(null));
    }

    // Test: refreshTokens should throw JwtExpiredException for expired token
    @Test
    @DisplayName("should throw JwtExpiredException for expired refresh token in refreshTokens")
    void refreshTokens_shouldThrowForExpiredToken() {
        doThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired")).when(jwtTokenProvider).validateRefreshToken(anyString());
        assertThrows(JwtExpiredException.class, () -> authService.refreshTokens("expired-token"));
    }

    // Test: refreshTokens should throw RuntimeException for other errors
    @Test
    @DisplayName("should throw RuntimeException for invalid refresh token in refreshTokens")
    void refreshTokens_shouldThrowForInvalidToken() {
        doThrow(new RuntimeException("invalid")).when(jwtTokenProvider).validateRefreshToken(anyString());
        assertThrows(RuntimeException.class, () -> authService.refreshTokens("invalid-token"));
    }

    @Test
    @DisplayName("should throw AccountVerificationException if sessionService returns false in refreshTokens")
    void refreshTokens_shouldThrowForInvalidSession() {
        when(jwtTokenProvider.getEmailFromRefreshToken(anyString())).thenReturn(EMAIL);
        when(sessionService.validateAndConsumeRefreshToken(anyString(), anyString())).thenReturn(false);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        doNothing().when(jwtTokenProvider).validateRefreshToken(anyString());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refreshTokens("valid-token"));
        assertTrue(ex.getCause() instanceof AccountVerificationException);
    }

    // Test: refreshTokens should return JwtAuthenticationResponse for valid token
    @Test
    @DisplayName("should return JwtAuthenticationResponse for valid refresh token")
    void refreshTokens_shouldReturnJwtAuthenticationResponse() {
        when(jwtTokenProvider.getEmailFromRefreshToken(anyString())).thenReturn(EMAIL);
        when(sessionService.validateAndConsumeRefreshToken(anyString(), anyString())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyList())).thenReturn(TOKEN);
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("new-refresh-token");
        doNothing().when(jwtTokenProvider).validateRefreshToken(anyString());
        JwtAuthenticationResponse response = authService.refreshTokens("valid-token");
        assertNotNull(response);
        assertEquals(TOKEN, response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }
}