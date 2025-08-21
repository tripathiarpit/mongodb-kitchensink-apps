package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.RedisValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.time.Duration;

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
    private SessionService sessionService;

    private final String EMAIL = "test@example.com";
    private final String SESSION_KEY = "SESSION:" + EMAIL;
    private final String OTP_KEY = "OTP:" + EMAIL;
    private final long OTP_EXPIRATION_SECONDS = 300L;
    private final long SESSION_EXPIRATION_SECONDS = 3600L;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(sessionService, "otpLength", 6);
        ReflectionTestUtils.setField(sessionService, "otpExpirationSeconds", OTP_EXPIRATION_SECONDS);
        ReflectionTestUtils.setField(sessionService, "sessionExpirationSeconds", SESSION_EXPIRATION_SECONDS);
        ReflectionTestUtils.setField(sessionService, "secureRandom", secureRandom);
    }

    // --- generateOtp Tests ---

    @Test
    @DisplayName("generateOtp should return existing OTP when not expired")
    void generateOtp_shouldReturnExistingOtpWhenNotExpired() {
        RedisValue<String> existingValue = mock(RedisValue.class);
        when(existingValue.isExpired()).thenReturn(false);
        when(existingValue.getValue()).thenReturn("123456");
        when(valueOperations.get(OTP_KEY)).thenReturn(existingValue);

        String result = sessionService.generateOtp(EMAIL);

        assertEquals("123456", result);
        verify(secureRandom, never()).nextInt(anyInt());
        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("generateOtp should generate new OTP when existing is expired")
    void generateOtp_shouldGenerateNewOtpWhenExpired() {
        RedisValue<String> existingValue = mock(RedisValue.class);
        when(existingValue.isExpired()).thenReturn(true);
        when(valueOperations.get(OTP_KEY)).thenReturn(existingValue);
        when(secureRandom.nextInt(anyInt())).thenReturn(123456);

        String result = sessionService.generateOtp(EMAIL);

        assertEquals("123456", result);
        verify(valueOperations, times(1)).set(eq(OTP_KEY), any(RedisValue.class), eq(Duration.ofSeconds(OTP_EXPIRATION_SECONDS)));
    }

    @Test
    @DisplayName("generateOtp should generate new OTP when no otp exists")
    void generateOtp_shouldGenerateNewOtpWhenNoOtpExists() {
        when(valueOperations.get(OTP_KEY)).thenReturn(null);
        when(secureRandom.nextInt(anyInt())).thenReturn(123456);

        String result = sessionService.generateOtp(EMAIL);

        assertEquals("123456", result);
        verify(valueOperations, times(1)).set(eq(OTP_KEY), any(RedisValue.class), eq(Duration.ofSeconds(OTP_EXPIRATION_SECONDS)));
    }

    // --- validateOtp Tests ---

    @Test
    @DisplayName("validateOtp should return true for valid, non-expired OTP")
    void validateOtp_shouldReturnTrueForValidOtp() {
        String otp = "123456";
        RedisValue<String> storedValue = mock(RedisValue.class);
        when(storedValue.isExpired()).thenReturn(false);
        when(storedValue.getValue()).thenReturn(otp);
        when(valueOperations.get(OTP_KEY)).thenReturn(storedValue);

        boolean result = sessionService.validateOtp(EMAIL, otp);

        assertTrue(result);
    }

    @Test
    @DisplayName("validateOtp should return false for invalid OTP")
    void validateOtp_shouldReturnFalseForInvalidOtp() {
        String otp = "123456";
        RedisValue<String> storedValue = mock(RedisValue.class);
        when(storedValue.isExpired()).thenReturn(false);
        when(storedValue.getValue()).thenReturn("654321");
        when(valueOperations.get(OTP_KEY)).thenReturn(storedValue);

        boolean result = sessionService.validateOtp(EMAIL, otp);

        assertFalse(result);
    }

    @Test
    @DisplayName("validateOtp should return false for expired OTP")
    void validateOtp_shouldReturnFalseForExpiredOtp() {
        String otp = "123456";
        RedisValue<String> storedValue = mock(RedisValue.class);
        when(storedValue.isExpired()).thenReturn(true);
        when(valueOperations.get(OTP_KEY)).thenReturn(storedValue);

        boolean result = sessionService.validateOtp(EMAIL, otp);

        assertFalse(result);
        verify(storedValue, never()).getValue();
    }

    @Test
    @DisplayName("validateOtp should return false for non-existent OTP")
    void validateOtp_shouldReturnFalseForNonExistentOtp() {
        when(valueOperations.get(OTP_KEY)).thenReturn(null);

        boolean result = sessionService.validateOtp(EMAIL, "123456");

        assertFalse(result);
    }

    // --- storeSessionToken Tests ---

    @Test
    @DisplayName("storeSessionToken should correctly store token")
    void storeSessionToken_shouldStoreTokenCorrectly() {
        String token = "testToken";

        sessionService.storeSessionToken(EMAIL, token);

        verify(valueOperations, times(1)).set(eq(SESSION_KEY), any(RedisValue.class), eq(Duration.ofSeconds(SESSION_EXPIRATION_SECONDS)));
    }

    // --- invalidateSessionToken & invalidateSession Tests ---

    @Test
    @DisplayName("invalidateSessionToken should delete the session key")
    void invalidateSessionToken_shouldDeleteSessionKey() {
        sessionService.invalidateSessionToken(EMAIL);

        verify(redisTemplate, times(1)).delete(SESSION_KEY);
    }

    @Test
    @DisplayName("invalidateSession should delete the session key")
    void invalidateSession_shouldDeleteSessionKey() {
        sessionService.invalidateSession(EMAIL);

        verify(redisTemplate, times(1)).delete(SESSION_KEY);
    }

    // --- validateSessionToken Tests ---

    @Test
    @DisplayName("validateSessionToken should return true and refresh for valid token")
    void validateSessionToken_shouldReturnTrueAndRefreshForValidToken() {
        String token = "validToken";
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(valueOperations.get(SESSION_KEY)).thenReturn(sessionValue);
        when(sessionValue.isExpired()).thenReturn(false);
        when(sessionValue.getValue()).thenReturn(token);

        boolean result = sessionService.validateSessionToken(EMAIL, token);

        assertTrue(result);
        verify(sessionValue, times(1)).refresh(SESSION_EXPIRATION_SECONDS);
        verify(valueOperations, times(1)).set(eq(SESSION_KEY), eq(sessionValue), eq(Duration.ofSeconds(SESSION_EXPIRATION_SECONDS)));
    }

    @Test
    @DisplayName("validateSessionToken should return false for invalid token value")
    void validateSessionToken_shouldReturnFalseForInvalidTokenValue() {
        String token = "invalidToken";
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(valueOperations.get(SESSION_KEY)).thenReturn(sessionValue);
        when(sessionValue.isExpired()).thenReturn(false);
        when(sessionValue.getValue()).thenReturn("correctToken");

        boolean result = sessionService.validateSessionToken(EMAIL, token);

        assertFalse(result);
        verify(sessionValue, never()).refresh(anyLong());
    }

    @Test
    @DisplayName("validateSessionToken should return false for expired token")
    void validateSessionToken_shouldReturnFalseForExpiredToken() {
        String token = "someToken";
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(valueOperations.get(SESSION_KEY)).thenReturn(sessionValue);
        when(sessionValue.isExpired()).thenReturn(true);

        boolean result = sessionService.validateSessionToken(EMAIL, token);

        assertFalse(result);
        verify(sessionValue, never()).getValue();
    }

    @Test
    @DisplayName("validateSessionToken should return false when token does not exist")
    void validateSessionToken_shouldReturnFalseWhenTokenDoesNotExist() {
        when(valueOperations.get(SESSION_KEY)).thenReturn(null);

        boolean result = sessionService.validateSessionToken(EMAIL, "someToken");

        assertFalse(result);
    }

    // --- doesSessionExist Tests ---

    @Test
    @DisplayName("doesSessionExist should return true for existing non-expired session")
    void doesSessionExist_shouldReturnTrueForExistingNonExpiredSession() {
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(valueOperations.get(SESSION_KEY)).thenReturn(sessionValue);
        when(sessionValue.isExpired()).thenReturn(false);

        boolean exists = sessionService.doesSessionExist(EMAIL);

        assertTrue(exists);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("doesSessionExist should return false and delete for expired session")
    void doesSessionExist_shouldReturnFalseAndDeleteForExpiredSession() {
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(valueOperations.get(SESSION_KEY)).thenReturn(sessionValue);
        when(sessionValue.isExpired()).thenReturn(true);

        boolean exists = sessionService.doesSessionExist(EMAIL);

        assertFalse(exists);
        verify(redisTemplate, times(1)).delete(SESSION_KEY);
    }

    @Test
    @DisplayName("doesSessionExist should return false for non-existent session")
    void doesSessionExist_shouldReturnFalseForNonExistentSession() {
        when(valueOperations.get(SESSION_KEY)).thenReturn(null);

        boolean exists = sessionService.doesSessionExist(EMAIL);

        assertFalse(exists);
        verify(redisTemplate, never()).delete(anyString());
    }

    // --- getTokenForExistingSession Tests ---

    @Test
    @DisplayName("getTokenForExistingSession should return token for non-expired session")
    void getTokenForExistingSession_shouldReturnTokenForNonExpiredSession() {
        String token = "existingToken";
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(valueOperations.get(SESSION_KEY)).thenReturn(sessionValue);
        when(sessionValue.isExpired()).thenReturn(false);
        when(sessionValue.getValue()).thenReturn(token);

        String result = sessionService.getTokenForExistingSession(EMAIL);

        assertEquals(token, result);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("getTokenForExistingSession should return null and delete for expired session")
    void getTokenForExistingSession_shouldReturnNullAndDeleteForExpiredSession() {
        RedisValue<String> sessionValue = mock(RedisValue.class);
        when(valueOperations.get(SESSION_KEY)).thenReturn(sessionValue);
        when(sessionValue.isExpired()).thenReturn(true);

        String result = sessionService.getTokenForExistingSession(EMAIL);

        assertNull(result);
        verify(redisTemplate, times(1)).delete(EMAIL);
    }
    @Test
    @DisplayName("getTokenForExistingSession should return null for non-existent session")
    void getTokenForExistingSession_shouldReturnNullForNonExistentSession() {
        when(valueOperations.get(SESSION_KEY)).thenReturn(null);
        String result = sessionService.getTokenForExistingSession(EMAIL);
        assertNull(result);
        verify(redisTemplate, never()).delete(anyString());
    }

}