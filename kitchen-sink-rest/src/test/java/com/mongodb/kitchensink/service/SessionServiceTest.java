package com.mongodb.kitchensink.service;
import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.RedisValue;
import com.mongodb.kitchensink.exception.JwtExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

import static com.mongodb.kitchensink.constants.AppContants.ACTIVE_ACCESS_TOKEN;
import static com.mongodb.kitchensink.constants.AppContants.REFRESH_TOKEN;
import static com.mongodb.kitchensink.constants.ErrorMessageConstants.TOKEN_EXPIRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Tests")
class SessionServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SecureRandom secureRandom;

    @InjectMocks
    @Spy
    private SessionService sessionService;

    private final String EMAIL = "test@example.com";
    private final String OTP_KEY = "OTP:" + EMAIL;
    private final String ACCESS_KEY = "ACTIVE_ACCESS_TOKEN:" + EMAIL;
    private final String REFRESH_KEY = "REFRESH_TOKEN:" + EMAIL;
    private final long OTP_EXPIRATION_SECONDS = 300L;
    private final long SESSION_EXPIRATION_SECONDS = 3600L;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(sessionService, "otpLength", 6);
        ReflectionTestUtils.setField(sessionService, "otpExpirationSeconds", OTP_EXPIRATION_SECONDS);
        ReflectionTestUtils.setField(sessionService, "refreshTokenExpirationSeconds", 7200L);
        ReflectionTestUtils.setField(sessionService, "secureRandom", secureRandom);
    }

    @Test
    @DisplayName("generateOtp returns existing OTP if not expired")
    void generateOtp_returnsExistingOtpIfNotExpired() {
        RedisValue<String> existing = mock(RedisValue.class);
        when(existing.isExpired()).thenReturn(false);
        when(existing.getValue()).thenReturn("123456");
        when(valueOperations.get(OTP_KEY)).thenReturn(existing);

        String result = sessionService.generateOtp(EMAIL);

        assertEquals("123456", result);
        verify(secureRandom, never()).nextInt(anyInt());
    }

    @Test
    @DisplayName("generateOtp generates new OTP if expired")
    void generateOtp_generatesNewOtpIfExpired() {
        RedisValue<String> existing = mock(RedisValue.class);
        when(existing.isExpired()).thenReturn(true);
        when(valueOperations.get(OTP_KEY)).thenReturn(existing);
        when(secureRandom.nextInt(anyInt())).thenReturn(654321);

        String result = sessionService.generateOtp(EMAIL);

        assertEquals("654321", result);
        verify(valueOperations).set(eq(OTP_KEY), any(RedisValue.class), eq(Duration.ofSeconds(OTP_EXPIRATION_SECONDS)));
    }

    @Test
    @DisplayName("generateOtp generates new OTP if none exists")
    void generateOtp_generatesNewOtpIfNoneExists() {
        when(valueOperations.get(OTP_KEY)).thenReturn(null);
        when(secureRandom.nextInt(anyInt())).thenReturn(111222);

        String result = sessionService.generateOtp(EMAIL);

        assertEquals("111222", result);
        verify(valueOperations).set(eq(OTP_KEY), any(RedisValue.class), eq(Duration.ofSeconds(OTP_EXPIRATION_SECONDS)));
    }

    @Test
    @DisplayName("validateOtp returns true for valid, non-expired OTP")
    void validateOtp_returnsTrueForValidOtp() {
        RedisValue<String> value = mock(RedisValue.class);
        when(value.isExpired()).thenReturn(false);
        when(value.getValue()).thenReturn("123456");
        when(valueOperations.get(OTP_KEY)).thenReturn(value);

        assertTrue(sessionService.validateOtp(EMAIL, "123456"));
    }

    @Test
    @DisplayName("validateOtp returns false for invalid OTP")
    void validateOtp_returnsFalseForInvalidOtp() {
        RedisValue<String> value = mock(RedisValue.class);
        when(value.isExpired()).thenReturn(false);
        when(value.getValue()).thenReturn("654321");
        when(valueOperations.get(OTP_KEY)).thenReturn(value);

        assertFalse(sessionService.validateOtp(EMAIL, "123456"));
    }

    @Test
    @DisplayName("validateOtp returns false for expired OTP")
    void validateOtp_returnsFalseForExpiredOtp() {
        RedisValue<String> value = mock(RedisValue.class);
        when(value.isExpired()).thenReturn(true);
        when(valueOperations.get(OTP_KEY)).thenReturn(value);

        assertFalse(sessionService.validateOtp(EMAIL, "123456"));
    }

    @Test
    @DisplayName("validateOtp returns false for non-existent OTP")
    void validateOtp_returnsFalseForNonExistentOtp() {
        when(valueOperations.get(OTP_KEY)).thenReturn(null);

        assertFalse(sessionService.validateOtp(EMAIL, "123456"));
    }

    @Test
    @DisplayName("storeAccessToken stores access token with correct key and duration")
    void storeAccessToken_storesToken() {
        String accessToken = "token";
        sessionService.storeAccessToken(EMAIL, accessToken, SESSION_EXPIRATION_SECONDS);
        ArgumentCaptor<RedisValue> captor = ArgumentCaptor.forClass(RedisValue.class);
        verify(valueOperations).set(eq(ACCESS_KEY), captor.capture(), eq(Duration.ofSeconds(SESSION_EXPIRATION_SECONDS)));
        RedisValue<String> capturedValue = captor.getValue();
        assertNotNull(capturedValue, "RedisValue should not be null");
        assertEquals(accessToken, capturedValue.getValue(), "The RedisValue should contain the correct token");
    }

    @Test
    @DisplayName("storeRefreshToken stores refresh token with correct key and duration")
    void storeRefreshToken_storesToken() {
        String refreshToken = "refresh";
        long expirationSeconds = 7200; // PT2H is 7200 seconds
        sessionService.storeRefreshToken(EMAIL, refreshToken, expirationSeconds);
        ArgumentCaptor<RedisValue> captor = ArgumentCaptor.forClass(RedisValue.class);
        verify(valueOperations).set(eq("REFRESH_TOKEN:" + EMAIL), captor.capture(), eq(Duration.ofSeconds(expirationSeconds)));
        RedisValue<String> capturedValue = captor.getValue();
        assertNotNull(capturedValue, "RedisValue should not be null");
        assertEquals(refreshToken, capturedValue.getValue(), "The RedisValue should contain the correct token");
    }

    @Test
    @DisplayName("invalidateSession deletes both access and refresh keys")
    void invalidateSession_deletesKeys() {
        doReturn(true).when(sessionService).doesSessionExist(EMAIL);
        sessionService.invalidateSession(EMAIL);
        verify(redisTemplate).delete(List.of(REFRESH_KEY, ACCESS_KEY));
    }

    @Test
    @DisplayName("validateSessionToken returns true if token matches stored")
    void validateSessionToken_returnsTrueIfTokenMatches() {
        when(valueOperations.get(ACCESS_KEY)).thenReturn("token");

        assertTrue(sessionService.validateSessionToken(EMAIL, "token"));
    }

    @Test
    @DisplayName("validateSessionToken returns false if token does not match")
    void validateSessionToken_returnsFalseIfTokenDoesNotMatch() {
        when(valueOperations.get(ACCESS_KEY)).thenReturn("other");

        assertFalse(sessionService.validateSessionToken(EMAIL, "token"));
    }

    @Test
    @DisplayName("doesSessionExist returns true for non-expired session")
    void doesSessionExist_returnsTrueForNonExpiredSession() {
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(sessionValue.isExpired()).thenReturn(false);
        when(valueOperations.get(ACCESS_KEY)).thenReturn(sessionValue);

        assertTrue(sessionService.doesSessionExist(EMAIL));
    }

    @Test
    @DisplayName("doesSessionExist returns false and deletes for expired session")
    void doesSessionExist_returnsFalseAndDeletesForExpiredSession() {
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(sessionValue.isExpired()).thenReturn(true);
        when(valueOperations.get(ACCESS_KEY)).thenReturn(sessionValue);

        assertFalse(sessionService.doesSessionExist(EMAIL));
        verify(redisTemplate).delete(ACCESS_KEY);
    }

    @Test
    @DisplayName("doesSessionExist returns false for non-existent session")
    void doesSessionExist_returnsFalseForNonExistentSession() {
        when(valueOperations.get(ACCESS_KEY)).thenReturn(null);

        assertFalse(sessionService.doesSessionExist(EMAIL));
    }

    @Test
    @DisplayName("getTokenForExistingSession returns token for non-expired session")
    void getTokenForExistingSession_returnsTokenForNonExpiredSession() {
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(sessionValue.isExpired()).thenReturn(false);
        when(sessionValue.getValue()).thenReturn("token");
        when(valueOperations.get(ACCESS_KEY)).thenReturn(sessionValue);

        assertEquals("token", sessionService.getTokenForExistingSession(EMAIL));
    }

    @Test
    @DisplayName("getTokenForExistingSession returns null and deletes for expired session")
    void getTokenForExistingSession_returnsNullAndDeletesForExpiredSession() {
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(sessionValue.isExpired()).thenReturn(true);
        when(valueOperations.get(ACCESS_KEY)).thenReturn(sessionValue);
        assertNull(sessionService.getTokenForExistingSession(EMAIL));
        verify(redisTemplate).delete(ACCESS_KEY);
    }

    @Test
    @DisplayName("getTokenForExistingSession returns null for non-existent session")
    void getTokenForExistingSession_returnsNullForNonExistentSession() {
        when(valueOperations.get(ACCESS_KEY)).thenReturn(null);

        assertNull(sessionService.getTokenForExistingSession(EMAIL));
    }
    @Test
    @DisplayName("invalidateSession should delete both access and refresh tokens if session exists")
    void invalidateSession_sessionExists_deletesTokens() {
        // Arrange
        String keyRefreshToken = REFRESH_TOKEN + ":" + EMAIL;
        String keyAccessToken = ACTIVE_ACCESS_TOKEN + ":" + EMAIL;

        // Mock doesSessionExist to return true so the delete branch is taken
        doReturn(true).when(sessionService).doesSessionExist(EMAIL);

        // Act
        sessionService.invalidateSession(EMAIL);

        // Assert
        verify(redisTemplate, times(1)).delete(List.of(keyRefreshToken, keyAccessToken));
    }

    @Test
    @DisplayName("invalidateSession should not delete tokens if session does not exist")
    void invalidateSession_sessionDoesNotExist_doesNotDeleteTokens() {
        // Arrange
        // Mock doesSessionExist to return false so the delete branch is skipped
        doReturn(false).when(sessionService).doesSessionExist(EMAIL);

        // Act
        sessionService.invalidateSession(EMAIL);

        // Assert
        verify(redisTemplate, never()).delete(anyList());
    }

    // --- Tests for validateSessionToken ---
    @Test
    @DisplayName("validateSessionToken should return false if accessToken is null")
    void validateSessionToken_accessTokenIsNull_returnsFalse() {
        // Act
        boolean result = sessionService.validateSessionToken(EMAIL, null);

        // Assert
        assertFalse(result);
        verify(valueOperations, never()).get(anyString()); // Should not even try to get from Redis
    }

    @Test
    @DisplayName("validateSessionToken should return false if stored token is null")
    void validateSessionToken_storedTokenIsNull_returnsFalse() {
        // Arrange
        String key = ACTIVE_ACCESS_TOKEN + ":" + EMAIL;
        when(valueOperations.get(key)).thenReturn(null); // Simulate no token in Redis

        // Act
        boolean result = sessionService.validateSessionToken(EMAIL, "ACCESS_TOKEN");

        // Assert
        assertFalse(result);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    @DisplayName("validateSessionToken should return false if accessToken does not match stored token")
    void validateSessionToken_tokenMismatch_returnsFalse() {
        // Arrange
        String key = ACTIVE_ACCESS_TOKEN + ":" + EMAIL;
        when(valueOperations.get(key)).thenReturn("mismatchedToken");
        boolean result = sessionService.validateSessionToken(EMAIL, "ACCESS_TOKEN");

        // Assert
        assertFalse(result);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    @DisplayName("validateSessionToken should return true if accessToken matches stored token")
    void validateSessionToken_tokenMatches_returnsTrue() {
        // Arrange
        String key = ACTIVE_ACCESS_TOKEN + ":" + EMAIL;
        when(valueOperations.get(key)).thenReturn("ACCESS_TOKEN");

        // Act
        boolean result = sessionService.validateSessionToken(EMAIL, "ACCESS_TOKEN");

        // Assert
        assertTrue(result);
        verify(valueOperations, times(1)).get(key);
    }

    // --- Tests for validateAndRefreshExistingSessionExpiry ---
    @Test
    @DisplayName("validateAndRefreshExistingSessionExpiry should throw JwtExpiredException if session does not exist")
    void validateAndRefreshExpiry_sessionDoesNotExist_throwsException() {
        // Arrange
        doReturn(false).when(sessionService).doesSessionExist(EMAIL);

        // Act & Assert
        JwtExpiredException exception = assertThrows(JwtExpiredException.class, () ->
                sessionService.validateAndRefreshExistingSessionExpiry(EMAIL, SESSION_EXPIRATION_SECONDS));

        assertEquals(ErrorCodes.SESSION_EXPIRED, exception.getErrorCode());
        assertEquals(TOKEN_EXPIRED, exception.getMessage());
        verify(valueOperations, never()).get(anyString());
        verify(sessionService, never()).storeAccessToken(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("validateAndRefreshExistingSessionExpiry should throw JwtExpiredException if session exists but stored RedisValue is null")
    void validateAndRefreshExpiry_storedRedisValueIsNull_throwsException() {
        // Arrange
        String key = ACTIVE_ACCESS_TOKEN + ":" + EMAIL;
        doReturn(true).when(sessionService).doesSessionExist(EMAIL);
        when(valueOperations.get(key)).thenReturn(null); // Simulate Redis entry gone bad or expired

        // Act & Assert
        JwtExpiredException exception = assertThrows(JwtExpiredException.class, () ->
                sessionService.validateAndRefreshExistingSessionExpiry(EMAIL, SESSION_EXPIRATION_SECONDS));

        assertEquals(ErrorCodes.SESSION_EXPIRED, exception.getErrorCode());
        assertEquals(TOKEN_EXPIRED, exception.getMessage());
        verify(redisTemplate, times(1)).delete(key); // Should attempt to delete the bad key
        verify(sessionService, never()).storeAccessToken(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("validateAndRefreshExistingSessionExpiry should refresh token expiry if session exists and RedisValue is valid")
    void validateAndRefreshExpiry_sessionExistsAndRedisValueValid_refreshesExpiry() {
        // Arrange
        String key = ACTIVE_ACCESS_TOKEN + ":" + EMAIL;
        RedisValue<String> existingSessionValue = new RedisValue<>("ACCESS_TOKEN", SESSION_EXPIRATION_SECONDS);

        // Use doReturn() to mock the spy's method call
        doReturn(true).when(sessionService).doesSessionExist(EMAIL);

        when(valueOperations.get(key)).thenReturn(existingSessionValue);

        // This is the crucial part: mock the internal call to storeAccessToken
        // Use doNothing() because it's a void method
        doNothing().when(sessionService).storeAccessToken(anyString(), anyString(), anyLong());

        // Act
        sessionService.validateAndRefreshExistingSessionExpiry(EMAIL, SESSION_EXPIRATION_SECONDS);

        // Assert
        // Verify that the mocked method was called exactly once with the correct arguments
        verify(sessionService, times(1)).storeAccessToken(EMAIL, "ACCESS_TOKEN", SESSION_EXPIRATION_SECONDS);
    }
}