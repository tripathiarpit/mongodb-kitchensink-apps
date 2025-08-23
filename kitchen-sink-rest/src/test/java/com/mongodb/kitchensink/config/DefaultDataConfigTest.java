package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.ProfileRepository;
import com.mongodb.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the DefaultDataConfig class.
 * This class uses Mockito to mock repository and password encoder dependencies,
 * and JUnit 5 for test execution. It verifies that the CommandLineRunner correctly
 * initializes default admin data.
 */
@ExtendWith(MockitoExtension.class)
class DefaultDataConfigTest {

    // Mock the UserRepository dependency
    @Mock
    private UserRepository userRepository;

    // Mock the PasswordEncoder dependency
    @Mock
    private PasswordEncoder passwordEncoder;

    // Mock the ProfileRepository dependency
    @Mock
    private ProfileRepository profileRepository;

    // Inject mocks into the DefaultDataConfig instance under test
    @InjectMocks
    private DefaultDataConfig defaultDataConfig;

    // To capture System.out.println output for verification
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private final String ADMIN_EMAIL = "admin@example.com";
    private final String ADMIN_PASSWORD = "admin";
    private final String ENCODED_PASSWORD = "encodedPassword123";

    /**
     * Set up necessary mocks and redirect System.out before each test.
     */
    @BeforeEach
    void setUp() {
        // Redirect System.out to capture console output
        System.setOut(new PrintStream(outContent));

        // Use ReflectionTestUtils to inject @Value fields for testing purposes
        // In a real Spring Boot test, these would be provided via @TestPropertySource
        ReflectionTestUtils.setField(defaultDataConfig, "adminEmail", ADMIN_EMAIL);
        ReflectionTestUtils.setField(defaultDataConfig, "adminPassword", ADMIN_PASSWORD);
    }

    /**
     * Restore original System.out after each test.
     */
    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    /**
     * Tests the scenario where the admin user does not exist in the database.
     * Verifies that a new admin user and profile are created and saved,
     * and the appropriate success message is printed to the console.
     */
    @Test
    @DisplayName("should create admin user and profile if they do not exist")
    void run_adminUserDoesNotExist_shouldCreateNewUserAndProfile() {
        // Given: userRepository.findByEmail returns an empty Optional (user does not exist)
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
        // Given: passwordEncoder encodes the admin password
        when(passwordEncoder.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        // Given: userRepository.save and profileRepository.save return the saved objects
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: the run method is executed
        assertDoesNotThrow(() -> defaultDataConfig.run());

        // Then:
        // Verify that findByEmail was called to check for existing admin
        verify(userRepository, times(1)).findByEmail(ADMIN_EMAIL);
        // Verify that passwordEncoder was used
        verify(passwordEncoder, times(1)).encode(ADMIN_PASSWORD);

        // Capture the User object saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Assert properties of the saved User
        assertNotNull(savedUser, "Saved user should not be null");
        assertEquals(ADMIN_EMAIL, savedUser.getEmail());
        assertEquals(ENCODED_PASSWORD, savedUser.getPasswordHash());
        assertTrue(savedUser.getRoles().contains("ADMIN"));
        assertTrue(savedUser.isActive());
        assertTrue(savedUser.isTwoFactorEnabled()); // Check for true as per DefaultDataConfig
        assertNull(savedUser.getTwoFactorSecret()); // Check for null as per DefaultDataConfig
        assertFalse(savedUser.getAccountVerificationPending());
        assertEquals("admin", savedUser.getUsername());
        assertNotNull(savedUser.getCreatedAt());

        // Capture the Profile object saved
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository, times(1)).save(profileCaptor.capture());
        Profile savedProfile = profileCaptor.getValue();

        // Assert properties of the saved Profile
        assertNotNull(savedProfile, "Saved profile should not be null");
        assertEquals("Admin", savedProfile.getFirstName());
        assertEquals("User", savedProfile.getLastName());
        assertEquals(ADMIN_EMAIL, savedProfile.getEmail());
        assertEquals("12345678", savedProfile.getPhoneNumber());
        assertNull(savedProfile.getAddress());
        assertEquals(savedUser.getUsername(), savedProfile.getUsername()); // Ensure username matches the user

        // Verify that the success message was printed
        assertTrue(outContent.toString().contains("✅ Default admin user created: " + ADMIN_EMAIL + " / password: admin"),
                "Success message not found in console output");
    }

    /**
     * Tests the scenario where the admin user already exists in the database.
     * Verifies that no new user or profile is created or saved,
     * and the appropriate message indicating existence is printed.
     */
    @Test
    @DisplayName("should not create admin user if it already exists")
    void run_adminUserAlreadyExists_shouldSkipCreation() {
        // Given: userRepository.findByEmail returns an Optional with an existing user
        User existingAdminUser = User.builder()
                .email(ADMIN_EMAIL)
                .username("admin")
                .passwordHash(ENCODED_PASSWORD)
                .twoFactorEnabled(true)
                .build();
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(existingAdminUser));

        // When: the run method is executed
        assertDoesNotThrow(() -> defaultDataConfig.run());

        // Then:
        // Verify that findByEmail was called to check for existing admin
        verify(userRepository, times(1)).findByEmail(ADMIN_EMAIL);
        // Verify that save on userRepository and profileRepository were NEVER called
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(Profile.class));
        // Verify that passwordEncoder was NEVER used
        verify(passwordEncoder, never()).encode(anyString());

        // Verify that the "already exists" message was printed
        assertTrue(outContent.toString().contains("Admin user already exists. Skipping creation."),
                "Admin exists message not found in console output");
    }
}
