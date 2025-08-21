package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppUserDetailsService Tests")
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    private final String EMAIL = "test@example.com";
    private final String PASSWORD_HASH = "encodedPasswordHash123";

    // --- loadUserByUsername Tests ---

    @Test
    @DisplayName("should return UserDetails for a valid user with a password")
    void loadUserByUsername_validUser_shouldReturnUserDetails() {
        // Given
        User mockUser = new User();
        mockUser.setEmail(EMAIL);
        mockUser.setPasswordHash(PASSWORD_HASH);
        mockUser.setRoles(Collections.singletonList("USER_ROLE"));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));

        // When
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(EMAIL);

        // Then
        assertNotNull(userDetails);
        assertEquals(EMAIL, userDetails.getUsername());
        assertEquals(PASSWORD_HASH, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER_ROLE")));
        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_userDoesNotExist_shouldThrowException() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            appUserDetailsService.loadUserByUsername(EMAIL);
        });
        assertEquals("User not found with email: " + EMAIL, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when password hash is null")
    void loadUserByUsername_passwordHashIsNull_shouldThrowException() {
        // Given
        User mockUser = new User();
        mockUser.setEmail(EMAIL);
        mockUser.setPasswordHash(null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            appUserDetailsService.loadUserByUsername(EMAIL);
        });
        assertEquals("Password not set for user: " + EMAIL, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when password hash is blank")
    void loadUserByUsername_passwordHashIsBlank_shouldThrowException() {
        // Given
        User mockUser = new User();
        mockUser.setEmail(EMAIL);
        mockUser.setPasswordHash("   ");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            appUserDetailsService.loadUserByUsername(EMAIL);
        });
        assertEquals("Password not set for user: " + EMAIL, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(EMAIL);
    }
}