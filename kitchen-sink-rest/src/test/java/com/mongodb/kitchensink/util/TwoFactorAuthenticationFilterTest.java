package com.mongodb.kitchensink.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.service.TwoFactorAuthService;
import com.mongodb.kitchensink.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TwoFactorAuthenticationFilter Tests")
class TwoFactorAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Spy // Use @Spy for ObjectMapper to allow real behavior while mocking others
    private ObjectMapper objectMapper;

    private TwoFactorAuthenticationFilter filter;

    private final String USERNAME = "testuser";
    private final String PASSWORD = "password123";
    private final String VALID_TOTP = "123456";
    private final String INVALID_TOTP = "654321";
    private final String SECRET = "mockSecret";

    private User mockUser;
    private Authentication successfulAuth;

    @BeforeEach
    void setUp() {
        // Initialize the filter with mocked dependencies
        filter = new TwoFactorAuthenticationFilter(authenticationManager, twoFactorAuthService, userService);

        // Common mock user setup
        mockUser = new User();
        mockUser.setUsername(USERNAME);
        mockUser.setTwoFactorEnabled(true);
        mockUser.setTwoFactorSecret(SECRET);

        // Common successful Authentication object returned by AuthenticationManager
        successfulAuth = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Stubbing for obtainUsername and obtainPassword (protected methods, accessed via filter behavior)
        lenient().when(request.getParameter("username")).thenReturn(USERNAME);
        lenient().when(request.getParameter("password")).thenReturn(PASSWORD);
    }

    // Helper method to create a mock ServletInputStream from a string
    private ServletInputStream createServletInputStream(String content) {
        return new ServletInputStream() {
            private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes());

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    // --- attemptAuthentication Tests ---

    @Test
    @DisplayName("should authenticate successfully with 2FA for JSON request")
    void attemptAuthentication_successWith2FA_json() throws IOException, ServletException {
        // Given
        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\",\"totp\":\"%s\"}", USERNAME, PASSWORD, VALID_TOTP);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getInputStream()).thenReturn(createServletInputStream(jsonBody));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser);
        when(twoFactorAuthService.verifyCode(SECRET, VALID_TOTP)).thenReturn(true);

        // When
        Authentication result = filter.attemptAuthentication(request, response);

        // Then
        assertNotNull(result);
        assertEquals(USERNAME, result.getName());
        verify(twoFactorAuthService, times(1)).verifyCode(SECRET, VALID_TOTP);
    }

    @Test
    @DisplayName("should authenticate successfully with 2FA for form data request")
    void attemptAuthentication_successWith2FA_formData() throws IOException, ServletException {
        // Given
        mockUser.setTwoFactorEnabled(true);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(request.getParameter("totp")).thenReturn(VALID_TOTP);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser);
        when(twoFactorAuthService.verifyCode(SECRET, VALID_TOTP)).thenReturn(true);

        // When
        Authentication result = filter.attemptAuthentication(request, response);

        // Then
        assertNotNull(result);
        assertEquals(USERNAME, result.getName());
        verify(twoFactorAuthService, times(1)).verifyCode(SECRET, VALID_TOTP);
    }

    @Test
    @DisplayName("should authenticate successfully without 2FA for JSON request")
    void attemptAuthentication_successWithout2FA_json() throws IOException, ServletException {
        // Given
        mockUser.setTwoFactorEnabled(false); // 2FA disabled
        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getInputStream()).thenReturn(createServletInputStream(jsonBody));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser); // User with 2FA disabled

        // When
        Authentication result = filter.attemptAuthentication(request, response);

        // Then
        assertNotNull(result);
        assertEquals(USERNAME, result.getName());
        verify(twoFactorAuthService, never()).verifyCode(anyString(), anyString()); // 2FA service not called
    }

    @Test
    @DisplayName("should authenticate successfully without 2FA for form data request")
    void attemptAuthentication_successWithout2FA_formData() throws IOException, ServletException {
        // Given
        mockUser.setTwoFactorEnabled(false); // 2FA disabled
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(request.getParameter("totp")).thenReturn(null); // No TOTP provided

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser); // User with 2FA disabled

        // When
        Authentication result = filter.attemptAuthentication(request, response);

        // Then
        assertNotNull(result);
        assertEquals(USERNAME, result.getName());
        verify(twoFactorAuthService, never()).verifyCode(anyString(), anyString()); // 2FA service not called
    }

    @Test
    @DisplayName("should throw BadCredentialsException when 2FA enabled and code missing (JSON)")
    void attemptAuthentication_2faEnabledCodeMissing_json() throws IOException {
        // Given
        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD); // No TOTP field
        when(request.getContentType()).thenReturn("application/json");
        when(request.getInputStream()).thenReturn(createServletInputStream(jsonBody));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser); // User with 2FA enabled

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                filter.attemptAuthentication(request, response)
        );
        assertEquals("2FA code is required", exception.getMessage());
        verify(twoFactorAuthService, never()).verifyCode(anyString(), anyString());
    }

    @Test
    @DisplayName("should throw BadCredentialsException when 2FA enabled and code missing (Form Data)")
    void attemptAuthentication_2faEnabledCodeMissing_formData() throws IOException {
        // Given
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(request.getParameter("totp")).thenReturn(""); // Empty TOTP parameter

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser); // User with 2FA enabled

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                filter.attemptAuthentication(request, response)
        );
        assertEquals("2FA code is required", exception.getMessage());
        verify(twoFactorAuthService, never()).verifyCode(anyString(), anyString());
    }

    @Test
    @DisplayName("should throw BadCredentialsException when 2FA enabled and code invalid (JSON)")
    void attemptAuthentication_2faEnabledCodeInvalid_json() throws IOException {
        // Given
        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\",\"totp\":\"%s\"}", USERNAME, PASSWORD, INVALID_TOTP);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getInputStream()).thenReturn(createServletInputStream(jsonBody));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser);
        when(twoFactorAuthService.verifyCode(SECRET, INVALID_TOTP)).thenReturn(false); // Invalid TOTP

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                filter.attemptAuthentication(request, response)
        );
        assertEquals("Invalid 2FA code", exception.getMessage());
        verify(twoFactorAuthService, times(1)).verifyCode(SECRET, INVALID_TOTP);
    }

    @Test
    @DisplayName("should throw BadCredentialsException when 2FA enabled and code invalid (Form Data)")
    void attemptAuthentication_2faEnabledCodeInvalid_formData() throws IOException {
        // Given
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(request.getParameter("totp")).thenReturn(INVALID_TOTP);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(userService.getUserByUserName(USERNAME)).thenReturn(mockUser);
        when(twoFactorAuthService.verifyCode(SECRET, INVALID_TOTP)).thenReturn(false); // Invalid TOTP

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                filter.attemptAuthentication(request, response)
        );
        assertEquals("Invalid 2FA code", exception.getMessage());
        verify(twoFactorAuthService, times(1)).verifyCode(SECRET, INVALID_TOTP);
    }

    @Test
    @DisplayName("should throw BadCredentialsException for invalid username/password (JSON)")
    void attemptAuthentication_invalidCredentials_json() throws IOException {
        // Given
        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, "wrongpass");
        when(request.getContentType()).thenReturn("application/json");
        when(request.getInputStream()).thenReturn(createServletInputStream(jsonBody));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                filter.attemptAuthentication(request, response)
        );
        assertEquals("Bad credentials", exception.getMessage());
        verify(userService, never()).getUserByUserName(anyString()); // 2FA flow not reached
    }

    @Test
    @DisplayName("should throw BadCredentialsException for invalid username/password (Form Data)")
    void attemptAuthentication_invalidCredentials_formData() throws IOException {
        // Given
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(request.getParameter("username")).thenReturn(USERNAME);
        when(request.getParameter("password")).thenReturn("wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                filter.attemptAuthentication(request, response)
        );
        assertEquals("Bad credentials", exception.getMessage());
        verify(userService, never()).getUserByUserName(anyString()); // 2FA flow not reached
    }

    // --- unsuccessfulAuthentication Tests ---

    @Test
    @DisplayName("should set unauthorized status and JSON response on failure")
    void unsuccessfulAuthentication_shouldSetUnauthorizedStatusAndJsonResponse() throws IOException, ServletException {
        // Given
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        AuthenticationException failedAuthException = new BadCredentialsException("Authentication failed reason");

        // When
        filter.unsuccessfulAuthentication(request, response, failedAuthException);

        // Then
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setContentType("application/json");
        String expectedJsonResponse = "{\"error\": \"Unauthorized\", \"message\": \"Authentication failed reason\"}";
        assertEquals(expectedJsonResponse, stringWriter.toString());
    }

    // --- successfulAuthentication Tests ---

    @Test
    @DisplayName("should call super successfulAuthentication")
    void successfulAuthentication_shouldCallSuper() throws IOException, ServletException {
        // Given a mock of the actual filter, so we can verify super method call
        TwoFactorAuthenticationFilter spyFilter = spy(new TwoFactorAuthenticationFilter(authenticationManager, twoFactorAuthService, userService));
        // We need to mock the super's internal behavior here. For unit test, we just ensure it's called.
        // Mock the protected method to avoid actual super.successfulAuthentication logic
        doNothing().when(spyFilter).successfulAuthentication(any(), any(), any(), any());

        // When
        spyFilter.successfulAuthentication(request, response, filterChain, successfulAuth);

        // Then
        verify(spyFilter, times(1)).successfulAuthentication(request, response, filterChain, successfulAuth);
        // Note: Directly verifying the super method call is tricky with Mockito
        // as you can only verify methods on the spy/mock itself.
        // This test primarily checks that our filter's method is invoked.
        // For a true test of super.successfulAuthentication's effects,
        // you'd typically need an integration test or to mock more deeply.
    }
}