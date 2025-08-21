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

        // 1. Initialize Objects First
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

        // 2. Then, Perform Stubbing with the Initialized Objects
        lenient().when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        lenient().when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        lenient().when(sessionService.doesSessionExist(anyString())).thenReturn(false);
    }
    @Test
    @DisplayName("should return successful response for valid credentials")
    void login_shouldReturnSuccessfulResponseForValidCredentials() throws Exception {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(sessionService.doesSessionExist(anyString())).thenReturn(false);
        when(jwtTokenProvider.generateToken(anyString(), anyList())).thenReturn(TOKEN);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(TOKEN, response.getToken());
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
        when(jwtTokenProvider.generateToken(anyString(), anyList())).thenReturn(TOKEN);
        LoginResponse response = authService.login(loginRequest);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNull(response.getToken());
        assertTrue(response.isAccountVerificationPending());
        assertTrue(response.isFirstLogin());
        verify(userService, times(1)).saveUpdatedUserAfterVerification(any(User.class));
    }

    @Test
    @DisplayName("should throw UserAuthException for bad credentials")
    void login_shouldThrowUserAuthExceptionForBadCredentials() {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid password"));

        // When & Then
        UserAuthException exception = assertThrows(UserAuthException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCodes.INVALID_CREDENTIALS, exception.getErrorCode());
    }

    // --- validateLoginRequest Tests ---

    @Test
    @DisplayName("should throw BadRequestException for null email")
    void validateLoginRequest_shouldThrowExceptionForNullEmail() {
        // Given
        LoginRequest invalidRequest = new LoginRequest(null, PASSWORD);
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(invalidRequest));
        assertEquals(REQ_EMAIL, exception.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException for invalid email format")
    void validateLoginRequest_shouldThrowExceptionForInvalidEmailFormat() {
        // Given
        LoginRequest invalidRequest = new LoginRequest("invalid-email", PASSWORD);
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.validateLoginRequest(invalidRequest));
        assertEquals(INVALID_EMAIL_FORMET, exception.getMessage());
    }

    // --- logout Tests ---

    @Test
    @DisplayName("should invalidate session token for a valid logout")
    void logout_shouldInvalidateSessionToken() throws Exception {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(sessionService.doesSessionExist(anyString())).thenReturn(true);
        // When
        authService.logout(EMAIL);
        // Then
        verify(sessionService, times(1)).invalidateSessionToken(EMAIL);
    }
    @Test
    @DisplayName("should not throw exception if session token does not exist")
    void logout_shouldNotThrowExceptionIfSessionDoesNotExist() throws Exception {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(sessionService.doesSessionExist(anyString())).thenReturn(false);
        // When & Then
        assertDoesNotThrow(() -> authService.logout(EMAIL));
        verify(sessionService, never()).invalidateSessionToken(anyString());
    }

    // --- validateSession Tests ---

    @Test
    @DisplayName("should return true for a valid session token")
    void validateSession_shouldReturnTrueForValidToken() {
        String authHeader = "Bearer " + TOKEN;
        doNothing().when(jwtTokenProvider).validateToken(anyString());
        when(jwtTokenProvider.getEmailFromToken(anyString())).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        boolean isValid = authService.validateSession(authHeader);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("should throw InvalidRequestException for invalid auth header format")
    void validateSession_shouldThrowExceptionForInvalidHeader() {
        // Given
        String authHeader = "Invalid " + TOKEN;
        // When & Then
        assertThrows(InvalidRequestException.class, () -> authService.validateSession(authHeader));
    }

    // --- getRolesByEmail Tests ---

    @Test
    @DisplayName("should return roles for valid email")
    void getRolesByEmail_shouldReturnRolesForValidEmail() {
        // Given
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        // When
        List<String> roles = authService.getRolesByEmail(EMAIL);
        // Then
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    @DisplayName("should throw UserNotFoundException for non-existent user email")
    void getRolesByEmail_shouldThrowUserNotFoundException() {
        // Given
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(null);
        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.getRolesByEmail(EMAIL));
        assertEquals(ErrorCodes.VALIDATION_ERROR, exception.getErrorCode());
    }

    // --- sendOtpForAccountVerification Tests ---

    @Test
    @DisplayName("should generate and send OTP for account verification")
    void sendOtpForAccountVerification_shouldSucceed() throws Exception {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(otpService.generateOtp(anyString(), anyString(), anyLong())).thenReturn("123456");
        // When
        assertDoesNotThrow(() -> authService.sendOtpForAccountVerification(EMAIL));
        // Then
        verify(otpService, times(1)).generateOtp(eq(EMAIL), eq(ACCOUNT_VERIFICATION), anyLong());
        verify(emailService, times(1)).sendEmail(eq(EMAIL), anyString(), anyString());
    }

    // --- verifyOtpForAccountVerification Tests ---

    @Test
    @DisplayName("should activate account successfully after OTP verification")
    void verifyOtpForAccountVerification_shouldSucceed() throws Exception {
        // Given
        OtpRequest otpRequest = new OtpRequest(EMAIL, "123456");
        when(otpService.verifyOtp(anyString(), anyString(), anyString())).thenReturn(true);
        // When
        ApiResponse response = authService.verifyOtpForAccountVerification(otpRequest);
        // Then
        assertTrue(response.isSuccess());
        assertEquals(OTP_VERIFIED_SUCCESS, response.getMessage());
        verify(userService, times(1)).activateAccount(eq(EMAIL), eq(true));
    }

    @Test
    @DisplayName("should throw InvalidOtpException for invalid OTP")
    void verifyOtpForAccountVerification_shouldThrowForInvalidOtp() {
        // Given
        OtpRequest otpRequest = new OtpRequest(EMAIL, "invalid_otp");
        when(otpService.verifyOtp(anyString(), anyString(), anyString())).thenReturn(false);
        // When & Then
        InvalidOtpException exception = assertThrows(InvalidOtpException.class, () -> authService.verifyOtpForAccountVerification(otpRequest));
        assertEquals(ErrorCodes.ACCOUNT_VERIFICATION_FAILED, exception.getErrorCode());
    }

    // --- getLoginResponse Tests ---

    @Test
    @DisplayName("should return login response for a verified user")
    void getLoginResponse_shouldReturnResponseForVerifiedUser() {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(sessionService.doesSessionExist(anyString())).thenReturn(true);
        when(sessionService.getTokenForExistingSession(anyString())).thenReturn(TOKEN);
        // When
        LoginResponse response = authService.getLoginResponse(EMAIL);
        // Then
        assertTrue(response.isSuccess());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    @DisplayName("should throw AccountVerificationExcpetion for a pending user")
    void getLoginResponse_shouldThrowForPendingUser() {
        // Given
        userDto.setAccountVerificationPending(true);
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);
        // When & Then
        AccountVerificationException exception = assertThrows(AccountVerificationException.class, () -> authService.getLoginResponse(EMAIL));
        assertEquals(ErrorCodes.ACCOUNT_VERIFICATION_PENDING, exception.getErrorCode());
    }
}