package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.exception.AccountVerificationException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.mapper.ProfileMapper;
import com.mongodb.kitchensink.mapper.UserMapper;
import com.mongodb.kitchensink.model.Address;
import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.ProfileRepository;
import com.mongodb.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static com.mongodb.kitchensink.constants.AppContants.ACCOUNT_VERIFICATION;
import static com.mongodb.kitchensink.constants.SuccessMessageConstants.ACCOUNT_CREATED_SUCCESSFULLY;
import static com.mongodb.kitchensink.constants.SuccessMessageConstants.ACCOUNT_DELETED_SUCCESSFULLY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private OtpService otpService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private UsernameGeneratorService usernameGeneratorService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserService userService;

    private User user;
    private Profile profile;
    private RegistrationRequest registrationRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "accountVerificationTtl", 300L);
        user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword")
                .roles(Collections.singletonList("ROLE_USER"))
                .active(true)
                .accountVerificationPending(true)
                .isFirstLogin(true)
                .twoFactorEnabled(true)
                .createdAt(Instant.now())
                .build();
        profile = new Profile();
        profile.setEmail("test@example.com");
        profile.setFirstName("Test");
        profile.setLastName("User");
        registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFirstName("Test");
        registrationRequest.setLastName("User");
        pageable = PageRequest.of(0, 10);
    }

    // --- registerUser Tests ---

    @Test
    @DisplayName("should register user successfully")
    void registerUser_shouldRegisterSuccessfully() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(usernameGeneratorService.generateUniqueUsername(anyString())).thenReturn("testuser");
        when(profileMapper.toProfile(any(RegistrationRequest.class))).thenReturn(profile);
        when(otpService.generateOtp(anyString(), eq(ACCOUNT_VERIFICATION), anyLong())).thenReturn("123456");
        Authentication authentication = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication)
                .getAuthorities();

        // When
        RegistrationResponse response = userService.registerUser(registrationRequest, authentication);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(ACCOUNT_CREATED_SUCCESSFULLY, response.getRegistrationMessage());
        verify(userRepository, times(1)).save(any(User.class));
        verify(profileRepository, times(1)).save(any(Profile.class));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("should return error if email already exists")
    void registerUser_shouldReturnErrorIfEmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        Authentication authentication = mock(Authentication.class);

        // When
        RegistrationResponse response = userService.registerUser(registrationRequest, authentication);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Email already registered", response.getRegistrationMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }
    @Test
    @DisplayName("should assign admin role to user when authenticated as admin")
    void registerUser_shouldAssignAdminRoleWhenAuthenticatedAsAdmin() {
        // Given
        registrationRequest.setRoles(List.of("ROLE_ADMIN"));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(usernameGeneratorService.generateUniqueUsername(anyString())).thenReturn("testuser");
        when(profileMapper.toProfile(any(RegistrationRequest.class))).thenReturn(profile);
        when(otpService.generateOtp(anyString(), eq(ACCOUNT_VERIFICATION), anyLong())).thenReturn("123456");

        Authentication authentication = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication)
                .getAuthorities();

        // When
        RegistrationResponse response = userService.registerUser(registrationRequest, authentication);

        // Then
        assertTrue(response.isSuccess());
        verify(userRepository).save(argThat(user -> user.getRoles().contains("ROLE_ADMIN")));
    }


    // --- getAllUsers Tests ---

    @Test
    @DisplayName("should return all users as paginated list")
    void getAllUsers_shouldReturnPaginatedList() {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(profileRepository.findByEmail(anyString())).thenReturn(Optional.of(profile));
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());
        when(userMapper.toDto(any(Profile.class))).thenReturn(new ProfileDto());

        // When
        Page<UserDto> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAll(pageable);
    }
    @Test
    @DisplayName("should return user list with null profile if no profile exists")
    void getAllUsers_shouldReturnUserListWithNullProfile() {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(profileRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        UserDto userDto = new UserDto();
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // When
        Page<UserDto> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertNull(result.getContent().get(0).getProfile());
    }

    // --- getAllUsersByEmailIds Tests ---

    @Test
    @DisplayName("should return all users for given email IDs")
    void getAllUsersByEmailIds_shouldReturnUsers() {
        // Given
        ArrayList<String> emails = new ArrayList<>(List.of("test@example.com"));
        when(userRepository.findByEmailIn(emails)).thenReturn(List.of(user));
        when(profileRepository.findByEmail(anyString())).thenReturn(Optional.of(profile));
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());
        when(userMapper.toDto(any(Profile.class))).thenReturn(new ProfileDto());

        // When
        List<UserDto> result = userService.getAllUsersByEmailIds(emails);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty email IDs")
    void getAllUsersByEmailIds_shouldReturnEmptyListForEmptyEmails() {
        // Given
        ArrayList<String> emails = new ArrayList<>();
        // When
        List<UserDto> result = userService.getAllUsersByEmailIds(emails);
        // Then
        assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("should return empty list for null email IDs")
    void getAllUsersByEmailIds_shouldReturnEmptyListForNullEmails() {
        // Given
        ArrayList<String> emails = null;
        // When
        List<UserDto> result = userService.getAllUsersByEmailIds(emails);
        // Then
        assertTrue(result.isEmpty());
    }

    // --- getUserById Tests ---

    @Test
    @DisplayName("should return user by id")
    void getUserById_shouldReturnUser() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());

        // When
        UserDto result = userService.getUserById("123");

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("should throw exception when user not found by id")
    void getUserById_shouldThrowExceptionWhenNotFound() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserById("123"));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.ACCOUNT_NOT_FOUND_ID, exception.getMessage());
    }

    // --- getUserByEmail Tests ---

    @Test
    @DisplayName("should return UserDto when user and profile exist")
    void getUserByEmail_shouldReturnUserDto() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(profileRepository.findByEmail(anyString())).thenReturn(Optional.of(profile));
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());
        when(userMapper.toDto(any(Profile.class))).thenReturn(new ProfileDto());

        // When
        UserDto result = userService.getUserByEmail(user.getEmail());

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(profileRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("should throw UserNotFoundException when user does not exist")
    void getUserByEmail_shouldThrowExceptionWhenUserDoesNotExist() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(user.getEmail()));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL, exception.getMessage());
    }

    // --- getUserByUserName Tests ---
    @Test
    @DisplayName("should return user by username")
    void getUserByUserName_shouldReturnUser() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserByUserName(user.getUsername());

        // Then
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    @DisplayName("should throw exception when user not found by username")
    void getUserByUserName_shouldThrowExceptionWhenNotFound() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserByUserName(user.getUsername()));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL, exception.getMessage());
    }

    // --- activateAccount Tests ---
    @Test
    @DisplayName("should activate account successfully")
    void activateAccount_shouldActivateSuccessfully() {
        // Given
        user.setAccountVerificationPending(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        // When
        userService.activateAccount(user.getEmail(), true);
        // Then
        assertFalse(user.getAccountVerificationPending());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("should throw AccountVerificationExcpetion when account is already verified")
    void activateAccount_shouldThrowExceptionWhenAlreadyVerified() {
        // Given
        user.setAccountVerificationPending(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        // When & Then
        AccountVerificationException exception = assertThrows(AccountVerificationException.class, () -> userService.activateAccount(user.getEmail(), false));
        assertEquals(ErrorCodes.ACCOUNT_VERIFICATION_FAILED, exception.getErrorCode());
    }

    // --- deleteUserByEmail Tests ---

    @Test
    @DisplayName("should delete user and profile successfully")
    void deleteUserByEmail_shouldDeleteSuccessfully() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(profileRepository.findByEmail(anyString())).thenReturn(Optional.of(profile));
        // When
        ResourceDeleteResponse response = userService.deleteUserByEmail(user.getEmail());
        System.out.println("response***************: " + response);
        assertTrue(response.isSuccess());
        assertEquals(ACCOUNT_DELETED_SUCCESSFULLY, response.getMessage());
        verify(userRepository, times(1)).delete(user);
        verify(profileRepository, times(1)).delete(profile);
        verify(redisTemplate, times(1)).delete("USER::" + user.getEmail());
    }
    @Test
    @DisplayName("should throw UserNotFoundException when deleting non-existent user")
    void deleteUserByEmail_shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.deleteUserByEmail(user.getEmail()));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL, exception.getMessage());
    }


    @Test
    @DisplayName("should return paginated user list by name")
    void getUsersByName_shouldReturnPaginatedList() {
        // Given
        Profile profileWithUsername = new Profile();
        profileWithUsername.setUsername("testuser"); // Set the username
        profileWithUsername.setEmail("test@example.com"); // Email is also used
        Page<Profile> profilePage = new PageImpl<>(List.of(profileWithUsername));

        when(profileRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(anyString(), anyString(), any(Pageable.class))).thenReturn(profilePage);

        // Stub the call with the correct username
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        when(profileMapper.toDto(any(Profile.class))).thenReturn(new ProfileDto());
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());

        // When
        Page<UserDto> result = userService.getUsersByName("Test", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
    @Test
    @DisplayName("should throw exception when no users found by name")
    void getUsersByName_shouldThrowExceptionWhenNotFound() {
        // Given
        Page<Profile> emptyPage = Page.empty();
        when(profileRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(anyString(), anyString(), any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUsersByName("Test", pageable));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.USERS_NOT_FOUND_BY_NAME, exception.getMessage());
    }

    // --- getUsersByEmail Tests ---
    @Test
    @DisplayName("should return paginated user list by email")
    void getUsersByEmail_shouldReturnPaginatedList() {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findByEmailContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(userPage);
        when(profileRepository.findByEmail(anyString())).thenReturn(Optional.of(profile));
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());
        when(profileMapper.toDto(any(Profile.class))).thenReturn(new ProfileDto());

        // When
        Page<UserDto> result = userService.getUsersByEmail("test@example.com", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
    @Test
    @DisplayName("should throw exception when user not found by email in getUsersByEmail")
    void getUsersByEmail_shouldThrowExceptionWhenProfileNotFound() {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findByEmailContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(userPage);
        when(profileRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUsersByEmail("test@example.com", pageable));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.USER_NOT_FOUND_EMAIL, exception.getMessage());
    }

    // --- getUsersByCountry Tests ---
    @Test
    @DisplayName("should return paginated user list by country")
    void getUsersByCountry_shouldReturnPaginatedList() {
        // Given
        // Create a new Profile with a username to avoid the NullPointerException
        Profile profileWithUsername = new Profile();
        profileWithUsername.setUsername("testuser");
        // Ensure email is set as it is used to find the User
        profileWithUsername.setEmail("test@example.com");

        Page<Profile> profilePage = new PageImpl<>(List.of(profileWithUsername));

        when(profileRepository.findByAddress_CountryContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(profilePage);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(profileMapper.toDto(any(Profile.class))).thenReturn(new ProfileDto());
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());

        // When
        Page<UserDto> result = userService.getUsersByCountry("Country", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
    @Test
    @DisplayName("should throw exception when no profiles found by country")
    void getUsersByCountry_shouldThrowExceptionWhenNotFound() {
        // Given
        Page<Profile> emptyPage = Page.empty();
        when(profileRepository.findByAddress_CountryContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(emptyPage);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUsersByCountry("Country", pageable));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.USERS_NOT_FOUND_BY_COUNTRY, exception.getMessage());
    }

    // --- updateUser Tests ---
    @Test
    @DisplayName("should update all user and profile fields correctly")
    void updateUser_shouldUpdateAllFieldsCorrectly() {
        User existingUser = User.builder()
                .email("old@example.com")
                .username("olduser")
                .active(true)
                .isFirstLogin(true)
                .twoFactorEnabled(false)
                .twoFactorSecret(null)
                .createdAt(Instant.now())
                .build();
        Profile existingProfile = new Profile();
        existingProfile.setEmail("old@example.com");
        existingProfile.setUsername("olduser");
        Address existingAddress = new Address();
        existingProfile.setAddress(existingAddress);
        UserDto updateRequest = new UserDto();
        updateRequest.setUsername("newuser");
        updateRequest.setActive(false);
        updateRequest.setFirstLogin(false);
        updateRequest.setAccountVerificationPending(false);
        updateRequest.setRoles(List.of("ROLE_ADMIN"));
        ProfileDto profileDto = new ProfileDto();
        profileDto.setFirstName("New");
        profileDto.setLastName("User");
        profileDto.setPhoneNumber("1234567890");
        profileDto.setStreet("New Street");
        profileDto.setCity("New City");
        profileDto.setState("New State");
        profileDto.setCountry("New Country");
        profileDto.setPincode("12345");
        updateRequest.setProfile(profileDto);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(profileRepository.findByUsername(anyString())).thenReturn(Optional.of(existingProfile));
        when(profileRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        // When
        UserDto result = userService.updateUser("old@example.com", updateRequest);

        // Then
        assertEquals("newuser", existingUser.getUsername());
        assertFalse(existingUser.isActive());
        assertFalse(existingUser.getFirstLogin());
        assertFalse(existingUser.getAccountVerificationPending());
        assertTrue(existingUser.getRoles().contains("ROLE_ADMIN"));

        assertEquals("New", existingProfile.getFirstName());
        assertEquals("New Street", existingProfile.getAddress().getStreet());
        verify(userRepository, times(1)).save(existingUser);
        verify(profileRepository, times(1)).save(existingProfile);
    }
    @Test
    @DisplayName("should throw exception when user to update is not found")
    void updateUser_shouldThrowExceptionWhenUserNotFound() {
        // Given
        UserDto updateRequest = new UserDto();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.updateUser("test@example.com", updateRequest));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.USERS_NOT_FOUND_BY_EMAIL, exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when profile to update is not found")
    void updateUser_shouldThrowExceptionWhenProfileNotFound() {
        // Given
        UserDto updateRequest = new UserDto();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(profileRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.updateUser("test@example.com", updateRequest));
        assertEquals(ErrorCodes.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.USERS_NOT_FOUND_BY_EMAIL, exception.getMessage());
    }
    @Test
    @DisplayName("should throw exception when new username already exists")
    void updateUser_shouldThrowExceptionWhenNewUsernameExists() {
        // Given
        User existingUser = User.builder()
                .email("test@example.com")
                .username("olduser")
                // Ensure all boolean fields are set to prevent NPE
                .active(true)
                .accountVerificationPending(false)
                .isFirstLogin(false)
                .twoFactorEnabled(false)
                .twoFactorSecret(null) // Can be null as it's an object
                .createdAt(Instant.now())
                .build();

        Profile existingProfile = new Profile();
        existingProfile.setUsername("olduser");

        UserDto updateRequest = new UserDto();
        updateRequest.setUsername("existinguser");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(profileRepository.findByUsername("olduser")).thenReturn(Optional.of(existingProfile));
        when(profileRepository.findByUsername("existinguser")).thenReturn(Optional.of(new Profile()));

        // When & Then
        AccountVerificationException exception = assertThrows(AccountVerificationException.class, () -> userService.updateUser("test@example.com", updateRequest));
        assertEquals(ErrorCodes.VALIDATION_ERROR, exception.getErrorCode());
        assertEquals(ErrorMessageConstants.USERS_FOUND_BY_USER_ID, exception.getMessage());
    }

}