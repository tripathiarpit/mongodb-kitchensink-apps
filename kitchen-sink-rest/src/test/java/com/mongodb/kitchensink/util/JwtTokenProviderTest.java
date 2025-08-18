package com.mongodb.kitchensink.util;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.exception.JwtExpiredException;
import io.jsonwebtoken.SignatureAlgorithm;
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
    private long validityInMs = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, validityInMs);
    }

    @Test
    @DisplayName("Should generate JWT token successfully")
    void testGenerateToken() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken("test@example.com", roles);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should validate a valid token")
    void testValidateTokenSuccess() {
        List<String> roles = Arrays.asList("ROLE_USER");
        String token = jwtTokenProvider.generateToken("test@example.com", roles);
        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should throw JwtExpiredException for expired token")
    void testValidateTokenExpired() throws InterruptedException {
        // Token with very short expiry (1 ms)
        JwtTokenProvider shortLivedTokenProvider = new JwtTokenProvider(secretKey, 1);
        String token = shortLivedTokenProvider.generateToken("test@example.com", Arrays.asList("ROLE_USER"));
        Thread.sleep(5); // let token expire

        JwtExpiredException exception = assertThrows(JwtExpiredException.class,
                () -> shortLivedTokenProvider.validateToken(token));

        assertEquals(ErrorCodes.VALIDATION_ERROR, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.TOKEN_EXPIRED, exception.getMessage());
    }

    @Test
    @DisplayName("Should extract username/email from token")
    void testGetEmailFromToken() {
        List<String> roles = Arrays.asList("ROLE_USER");
        String token = jwtTokenProvider.generateToken("test@example.com", roles);

        String email = jwtTokenProvider.getEmailFromToken(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("test@example.com", email);
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Should extract roles from token")
    void testGetRolesFromToken() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken("test@example.com", roles);

        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ROLE_USER"));
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should throw RuntimeException for invalid token")
    void testValidateTokenInvalid() {
        String invalidToken = "invalid.token.here";
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtTokenProvider.validateToken(invalidToken));

        assertEquals(ErrorMessageConstants.INVALID_OR_EXPIRED_SESSION, exception.getMessage());
    }
}
