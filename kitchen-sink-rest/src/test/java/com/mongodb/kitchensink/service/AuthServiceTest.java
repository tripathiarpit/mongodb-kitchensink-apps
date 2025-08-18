package com.mongodb.kitchensink.service;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
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

    private UserDto testUserDto;
    private User testUser;
    private Authentication mockAuthentication;
    private UserDetails mockUserDetails;
    private ProfileDto testProfile;
    private String email="admin@example.com";
    private String password="Admin@123";
    @BeforeEach
    void setUp() {
        // Setup profile
        testProfile = new ProfileDto();
        testProfile.setFirstName("Admin");
        testProfile.setLastName("Admin");

        // Setup DTO
        testUserDto = new UserDto();
        testUserDto.setEmail(this.email);
        testUserDto.setProfile(testProfile);
        testUserDto.setAccountVerificationPending(false);
        testUserDto.setFirstLogin(false);

        // Authorities
        List<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("USER"),
                new SimpleGrantedAuthority("ADMIN")
        );

        // Use real Authentication object
        mockAuthentication = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "Admin@123",
                authorities
        );

        // Mock UserDetails
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password(this.password)
                .authorities(authorities)
                .build();
    }


    @Test
    @DisplayName("Should login successfully for valid credentials")
    void testLoginSuccess() throws Exception {
        when(userService.getUserByEmail(this.email)).thenReturn(testUserDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(sessionService.doesSessionExist(this.email)).thenReturn(false);
        when(jwtTokenProvider.generateToken(eq(this.email), anyList())).thenReturn("mock-token");

        LoginResponse response = authService.login(this.email, this.password);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("mock-token", response.getToken());
        verify(sessionService).storeSessionToken(eq(this.email), eq("mock-token"));
    }

    @Test
    @DisplayName("Should throw UserAuthException for bad credentials")
    void testLoginBadCredentials() throws Exception {
        when(userService.getUserByEmail(this.email)).thenReturn(testUserDto);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(UserAuthException.class, () -> authService.login(this.email, "wrongpass"));
    }

    @Test
    @DisplayName("Should logout and invalidate session")
    void testLogout() throws Exception {
        when(userService.getUserByEmail(this.email)).thenReturn(testUserDto);
        when(sessionService.doesSessionExist(this.email)).thenReturn(true);

        authService.logout(this.email);

        verify(sessionService).invalidateSessionToken(this.email);
    }

    @Test
    @DisplayName("Should validate session successfully")
    void testValidateSession() {
        String authHeader = "Bearer mock-token";

        // void method: use doNothing
        doNothing().when(jwtTokenProvider).validateToken("mock-token");
        when(jwtTokenProvider.getEmailFromToken("mock-token")).thenReturn(this.email);
        when(userDetailsService.loadUserByUsername(this.email)).thenReturn(mockUserDetails);

        boolean isValid = authService.validateSession(authHeader);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return roles from token")
    void testGetRolesFromToken() {
        String authHeader = "Bearer mock-token";
        doNothing().when(jwtTokenProvider).validateToken("mock-token");
        when(jwtTokenProvider.getEmailFromToken("mock-token")).thenReturn(this.email);
        when(userDetailsService.loadUserByUsername(this.email)).thenReturn(mockUserDetails);

        List<String> roles = authService.getRolesFromToken(authHeader);

        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    @DisplayName("Should send OTP for account verification")
    void testSendOtpForAccountVerification() throws Exception {
        when(userService.getUserByEmail(this.email)).thenReturn(testUserDto);
        when(otpService.generateOtp(eq(this.email), eq("ACCOUNT_VERIFICATION"), anyLong()))
                .thenReturn("123456");

        authService.sendOtpForAccountVerification(this.email);

        verify(emailService).sendEmail(
                eq(this.email),
                anyString(),
                contains("123456")
        );
    }

    @Test
    @DisplayName("Should verify OTP for account verification")
    void testVerifyOtpForAccountVerification() throws Exception {
        OtpRequest request = new OtpRequest();
        request.setEmail(this.email);
        request.setOtp("123456");

        when(otpService.verifyOtp(this.email, "ACCOUNT_VERIFICATION", "123456")).thenReturn(true);

        ApiResponse response = authService.verifyOtpForAccountVerification(request);

        assertTrue(response.isSuccess());
        assertEquals("OTP verified successfully", response.getMessage());
        verify(userService).activateAccount(this.email, true);
    }
}
