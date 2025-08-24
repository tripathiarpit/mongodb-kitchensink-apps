package com.mongodb.kitchensink.util;

import com.mongodb.kitchensink.config.JwtAuthenticationEntryPoint;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.UserRepository;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

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
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("should set authentication for a valid JWT and valid session")
    void doFilterInternal_validTokenAndSession_shouldSetAuthentication() throws ServletException, IOException {
        when(request.getHeader(AUTH_HEADER)).thenReturn(VALID_TOKEN);
        doNothing().when(tokenProvider).validateAccessToken(VALID_JWT);
        when(tokenProvider.getEmailFromAccessToken(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(valueOperations.get("ACTIVE_ACCESS_TOKEN:" + TEST_EMAIL)).thenReturn(VALID_JWT);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

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
        when(request.getHeader(AUTH_HEADER)).thenReturn(VALID_TOKEN);
        doNothing().when(tokenProvider).validateAccessToken(VALID_JWT);
        when(tokenProvider.getEmailFromAccessToken(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(valueOperations.get("ACTIVE_ACCESS_TOKEN:" + TEST_EMAIL)).thenReturn(VALID_JWT);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtAuthenticationEntryPoint, times(1)).commence(any(), any(), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("should not set authentication if session is invalid")
    void doFilterInternal_invalidSession_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader(AUTH_HEADER)).thenReturn(VALID_TOKEN);
        doNothing().when(tokenProvider).validateAccessToken(VALID_JWT);
        when(tokenProvider.getEmailFromAccessToken(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(valueOperations.get("ACTIVE_ACCESS_TOKEN:" + TEST_EMAIL)).thenReturn(null); // Simulate missing/expired session

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtAuthenticationEntryPoint, times(1)).commence(any(), any(), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("should do nothing if Authorization header is missing")
    void doFilterInternal_noHeader_shouldDoNothing() throws ServletException, IOException {
        when(request.getHeader(AUTH_HEADER)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenProvider, never()).validateAccessToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("should not filter login requests")
    void shouldNotFilter_loginRequest_shouldReturnTrue() {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        assertTrue(result);
    }

    @Test
    @DisplayName("should not filter user registration requests")
    void shouldNotFilter_registerRequest_shouldReturnTrue() {
        when(request.getRequestURI()).thenReturn("/api/users/register");
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        assertTrue(result);
    }

    @Test
    @DisplayName("should not filter logout requests")
    void shouldNotFilter_logoutRequest_shouldReturnTrue() {
        when(request.getRequestURI()).thenReturn("/api/auth/logout");
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        assertTrue(result);
    }

    @Test
    @DisplayName("should filter other requests")
    void shouldNotFilter_otherRequest_shouldReturnFalse() {
        when(request.getRequestURI()).thenReturn("/api/users/profile");
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);
        assertFalse(result);
    }
}