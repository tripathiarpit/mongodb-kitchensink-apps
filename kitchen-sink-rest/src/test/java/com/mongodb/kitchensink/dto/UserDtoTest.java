package com.mongodb.kitchensink.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.kitchensink.dto.ErrorResponse;
import com.mongodb.kitchensink.dto.UserDto;
import com.mongodb.kitchensink.dto.ProfileDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDtoTest {

    private UserDto userDto;
    private ProfileDto profileDto;

    @BeforeEach
    public void setUp() {
        profileDto = new ProfileDto(); // Assuming ProfileDto exists and can be instantiated
        userDto = new UserDto();
    }

    @Test
    @DisplayName("Test default constructor")
    public void testDefaultConstructor() {
        assertNotNull(userDto);
        assertFalse(userDto.isTwoFactorEnabled());
    }

    @Test
    @DisplayName("Test all-args constructor")
    public void testAllArgsConstructor() {
        String id = "123";
        String email = "test@example.com";
        String username = "testuser";
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        boolean active = true;
        Instant createdAt = Instant.now();
        Boolean isAccountVerificationPending = false;
        Boolean isFirstLogin = false;
        String twoFactorSecret = "secret123";
        boolean twoFactorEnabled = true;
        ProfileDto profile = new ProfileDto(); // Assuming ProfileDto exists

        UserDto allArgsConstructorUser = new UserDto(id, email, username, roles, active, createdAt, isAccountVerificationPending, isFirstLogin, twoFactorSecret, twoFactorEnabled, profile);

        assertEquals(id, allArgsConstructorUser.getId());
        assertEquals(email, allArgsConstructorUser.getEmail());
        assertEquals(username, allArgsConstructorUser.getUsername());
        assertEquals(roles, allArgsConstructorUser.getRoles());
        assertEquals(active, allArgsConstructorUser.isActive());
        assertEquals(createdAt, allArgsConstructorUser.getCreatedAt());
        assertEquals(isAccountVerificationPending, allArgsConstructorUser.getAccountVerificationPending());
        assertEquals(isFirstLogin, allArgsConstructorUser.getFirstLogin());
        assertEquals(twoFactorSecret, allArgsConstructorUser.getTwoFactorSecret());
        assertEquals(twoFactorEnabled, allArgsConstructorUser.isTwoFactorEnabled());
        assertEquals(profile, allArgsConstructorUser.getProfile());
    }

    @Test
    @DisplayName("Test getId and setId")
    public void testId() {
        String id = "testId1";
        userDto.setId(id);
        assertEquals(id, userDto.getId());
    }

    @Test
    @DisplayName("Test getEmail and setEmail")
    public void testEmail() {
        String email = "test@domain.com";
        userDto.setEmail(email);
        assertEquals(email, userDto.getEmail());
    }

    @Test
    @DisplayName("Test getUsername and setUsername")
    public void testUsername() {
        String username = "testuser1";
        userDto.setUsername(username);
        assertEquals(username, userDto.getUsername());
    }

    @Test
    @DisplayName("Test getRoles and setRoles")
    public void testRoles() {
        List<String> roles = Arrays.asList("ADMIN", "USER");
        userDto.setRoles(roles);
        assertEquals(roles, userDto.getRoles());
    }

    @Test
    @DisplayName("Test isActive and setActive")
    public void testActive() {
        userDto.setActive(true);
        assertTrue(userDto.isActive());
    }

    @Test
    @DisplayName("Test getCreatedAt and setCreatedAt")
    public void testCreatedAt() {
        Instant now = Instant.now();
        userDto.setCreatedAt(now);
        assertEquals(now, userDto.getCreatedAt());
    }

    @Test
    @DisplayName("Test getProfile and setProfile")
    public void testProfile() {
        ProfileDto newProfile = new ProfileDto();
        userDto.setProfile(newProfile);
        assertEquals(newProfile, userDto.getProfile());
    }

    @Test
    @DisplayName("Test getAccountVerificationPending and setAccountVerificationPending")
    public void testAccountVerificationPending() {
        userDto.setAccountVerificationPending(true);
        assertTrue(userDto.getAccountVerificationPending());
    }

    @Test
    @DisplayName("Test getFirstLogin and setFirstLogin")
    public void testFirstLogin() {
        userDto.setFirstLogin(true);
        assertTrue(userDto.getFirstLogin());
    }

    @Test
    @DisplayName("Test getTwoFactorSecret and setTwoFactorSecret")
    public void testTwoFactorSecret() {
        String secret = "newSecret";
        userDto.setTwoFactorSecret(secret);
        assertEquals(secret, userDto.getTwoFactorSecret());
    }

    @Test
    @DisplayName("Test isTwoFactorEnabled and setTwoFactorEnabled")
    public void testTwoFactorEnabled() {
        userDto.setTwoFactorEnabled(true);
        assertTrue(userDto.isTwoFactorEnabled());
    }
}
