package com.mongodb.kitchensink.util;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.exception.JwtExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String secretKey = "MySuperSecretKeyForJwtGeneration12345"; // must be at least 256 bits for HS256
    private long validityInSeconds = 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, validityInSeconds, validityInSeconds);
    }

    @Test
    @DisplayName("Should generate access token successfully")
    void testGenerateAccessToken() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken("test@example.com", roles);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void testGenerateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken("test@example.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should validate a valid access token")
    void testValidateAccessTokenSuccess() {
        List<String> roles = Arrays.asList("USER");
        String token = jwtTokenProvider.generateAccessToken("test@example.com", roles);
        assertDoesNotThrow(() -> jwtTokenProvider.validateAccessToken(token));
    }

    @Test
    @DisplayName("Should validate a valid refresh token")
    void testValidateRefreshTokenSuccess() {
        String token = jwtTokenProvider.generateRefreshToken("test@example.com");
        assertDoesNotThrow(() -> jwtTokenProvider.validateRefreshToken(token));
    }

    @Test
    @DisplayName("Should throw JwtExpiredException for expired access token")
    void testValidateAccessTokenExpired() throws InterruptedException {
        JwtTokenProvider shortLivedTokenProvider = new JwtTokenProvider(secretKey, 1, 1);
        String token = shortLivedTokenProvider.generateAccessToken("test@example.com", Arrays.asList("USER"));
        Thread.sleep(1100); // Sleep for 1.1 seconds to ensure expiration
        JwtExpiredException exception = assertThrows(JwtExpiredException.class,
                () -> shortLivedTokenProvider.validateAccessToken(token));
        assertEquals(ErrorCodes.VALIDATION_ERROR, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.TOKEN_EXPIRED, exception.getMessage());
    }
    @Test
    @DisplayName("Should throw JwtExpiredException for expired refresh token")
    void testValidateRefreshTokenExpired() throws InterruptedException {
        JwtTokenProvider shortLivedTokenProvider = new JwtTokenProvider(secretKey, 1, 1);
        String token = shortLivedTokenProvider.generateRefreshToken("test@example.com");
        Thread.sleep(1100); // Sleep for 1.1 seconds to ensure expiration
        JwtExpiredException exception = assertThrows(JwtExpiredException.class,
                () -> shortLivedTokenProvider.validateRefreshToken(token));
        assertEquals(ErrorCodes.SESSION_EXPIRED, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.TOKEN_EXPIRED, exception.getMessage());
    }

    @Test
    @DisplayName("Should extract email from access token")
    void testGetEmailFromAccessToken() {
        List<String> roles = Arrays.asList("USER");
        String token = jwtTokenProvider.generateAccessToken("test@example.com", roles);
        String email = jwtTokenProvider.getEmailFromAccessToken(token);
        assertEquals("test@example.com", email);
    }

    @Test
    @DisplayName("Should extract email from refresh token")
    void testGetEmailFromRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken("test@example.com");
        String email = jwtTokenProvider.getEmailFromRefreshToken(token);
        assertEquals("test@example.com", email);
    }

    @Test
    @DisplayName("Should extract roles from access token")
    void testGetRolesFromToken() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken("test@example.com", roles);
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("USER"));
        assertTrue(extractedRoles.contains("ADMIN"));
    }

    @Test
    @DisplayName("Should throw RuntimeException for invalid access token")
    void testValidateAccessTokenInvalid() {
        String invalidToken = "invalid.token.here";
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtTokenProvider.validateAccessToken(invalidToken));
        assertEquals(ErrorMessageConstants.INVALID_OR_EXPIRED_SESSION, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw JwtExpiredException for invalid refresh token")
    void testValidateRefreshTokenInvalid() {
        String invalidToken = "invalid.token.here";
        JwtExpiredException exception = assertThrows(JwtExpiredException.class,
                () -> jwtTokenProvider.validateRefreshToken(invalidToken));
        assertEquals(ErrorCodes.INVALID_JWT_TOKEN, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.TOKEN_INVALID, exception.getMessage());
    }
}