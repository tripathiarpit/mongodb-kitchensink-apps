package com.mongodb.kitchensink.util;

import com.mongodb.kitchensink.config.JwtAuthenticationEntryPoint;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.UserRepository;
import com.mongodb.kitchensink.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String AUTH_HEADER = "Authorization";
    private static final String VALID_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eS.l";
    private static final String VALID_JWT = VALID_TOKEN.substring(7);
    private static final String TEST_EMAIL = "test@example.com";

    private User mockUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockUser = new User();
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setRoles(Collections.singletonList("USER"));
        mockUser.setActive(true);
    }

    // --- doFilterInternal Success Tests ---

    @Test
    @DisplayName("should set authentication for a valid JWT and valid session")
    void doFilterInternal_validTokenAndSession_shouldSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader(AUTH_HEADER)).thenReturn(VALID_TOKEN);
        doNothing().when(tokenProvider).validateToken(VALID_JWT);
        when(tokenProvider.getEmailFromToken(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(sessionService.validateSessionToken(TEST_EMAIL, VALID_JWT)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(TEST_EMAIL, authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());
        List<String> authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority()).collect(Collectors.toList());
        assertTrue(authorities.contains("ROLE_USER"));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("should not set authentication if user is not found")
    void doFilterInternal_userNotFound_shouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader(AUTH_HEADER)).thenReturn(VALID_TOKEN);
        doNothing().when(tokenProvider).validateToken(VALID_JWT);
        when(tokenProvider.getEmailFromToken(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtAuthenticationEntryPoint, times(1)).commence(any(), any(), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("should not set authentication if session is invalid")
    void doFilterInternal_invalidSession_shouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader(AUTH_HEADER)).thenReturn(VALID_TOKEN);
        doNothing().when(tokenProvider).validateToken(VALID_JWT);
        when(tokenProvider.getEmailFromToken(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(sessionService.validateSessionToken(TEST_EMAIL, VALID_JWT)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtAuthenticationEntryPoint, times(1)).commence(any(), any(), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("should do nothing if Authorization header is missing")
    void doFilterInternal_noHeader_shouldDoNothing() throws ServletException, IOException {
        // Given
        when(request.getHeader(AUTH_HEADER)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenProvider, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // --- shouldNotFilter Tests ---

    @Test
    @DisplayName("should not filter login requests")
    void shouldNotFilter_loginRequest_shouldReturnTrue() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("should not filter user registration requests")
    void shouldNotFilter_registerRequest_shouldReturnTrue() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users/register");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("should filter other requests")
    void shouldNotFilter_otherRequest_shouldReturnFalse() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users/profile");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertFalse(result);
    }
}