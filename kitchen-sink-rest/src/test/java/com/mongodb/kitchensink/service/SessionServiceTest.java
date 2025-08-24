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
import java.util.List;

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
        sessionService.storeAccessToken(EMAIL, "token", SESSION_EXPIRATION_SECONDS);

        verify(valueOperations).set(eq(ACCESS_KEY), eq("token"), eq(Duration.ofSeconds(SESSION_EXPIRATION_SECONDS)));
    }

    @Test
    @DisplayName("storeRefreshToken stores refresh token with correct key and duration")
    void storeRefreshToken_storesToken() {
        sessionService.storeRefreshToken(EMAIL, "refresh", 7200L);

        verify(valueOperations).set(eq(REFRESH_KEY), eq("refresh"), eq(Duration.ofSeconds(7200L)));
    }

    @Test
    @DisplayName("invalidateSession deletes both access and refresh keys")
    void invalidateSession_deletesKeys() {
        sessionService.invalidateSession(EMAIL);

        verify(redisTemplate).delete(List.of("REFRESH_TOKEN" + EMAIL, "ACTIVE_ACCESS_TOKEN" + EMAIL));
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
}