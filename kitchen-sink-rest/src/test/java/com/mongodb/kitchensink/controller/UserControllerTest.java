package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.service.DownloadFileService;
import com.mongodb.kitchensink.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private DownloadFileService downloadFileService;

    @InjectMocks
    private UserController userController;

    private Authentication mockAuthentication;
    private UserDto testUserDto;
    private Page<UserDto> testUserPage;
    private String email = "admin@example.com";

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setEmail(email);
        ProfileDto profile = new ProfileDto();
        profile.setFirstName("John");
        profile.setLastName("Doe");
        testUserDto.setProfile(profile);
        testUserPage = new PageImpl<>(Collections.singletonList(testUserDto));
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser() {
        // Given
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail(this.email);

        RegistrationResponse expectedResponse = new RegistrationResponse(true, "User registered successfully");
        when(userService.registerUser(any(RegistrationRequest.class), any()))
                .thenReturn(expectedResponse);

        ResponseEntity<RegistrationResponse> result = userController.registerUser(request, null);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());

        verify(userService).registerUser(any(RegistrationRequest.class), any());
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void testGetAllUsers() {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(testUserPage);

        ResponseEntity<Page<UserDto>> result = userController.getAllUsers(0, 50, "createdAt", "asc");
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals(email, result.getBody().getContent().get(0).getEmail());
    }

    @Test
    @DisplayName("Should get user by ID")
    void testGetUserById() {
        when(userService.getUserById("123")).thenReturn(testUserDto);

        ResponseEntity<UserDto> result = userController.getUserById("123");
        assertEquals(email, result.getBody().getEmail());
    }

    @Test
    @DisplayName("Should get user by email")
    void testGetUserByEmail() {
        when(userService.getUserByEmail(email)).thenReturn(testUserDto);

        ResponseEntity<UserDto> result = userController.getUserByEmail(email);
        assertEquals(email, result.getBody().getEmail());
    }

    @Test
    @DisplayName("Should search users by city")
    void testGetUsersByCity() {
        when(userService.getUsersByCity(eq("New York"), any(Pageable.class))).thenReturn(testUserPage);

        ResponseEntity<Page<UserDto>> result = userController.getUsersByCity("New York", 0, 50);
        assertEquals(1, result.getBody().getContent().size());
        assertEquals(email, result.getBody().getContent().get(0).getEmail());
    }

    @Test
    @DisplayName("Should delete user by email")
    void testDeleteUserByEmail() {
        ResourceDeleteResponse response = new ResourceDeleteResponse(true, "User deleted successfully");
        when(userService.deleteUserByEmail(email)).thenReturn(response);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ResponseEntity<ResourceDeleteResponse> result = userController.deleteUserByEmail(request);
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should update user")
    void testUpdateUser() {
        when(userService.updateUser("123", testUserDto)).thenReturn(testUserDto);

        ResponseEntity<UserDto> result = userController.updateUser("123", testUserDto);
        assertEquals(email, result.getBody().getEmail());
    }

    @Test
    @DisplayName("Should download users as Excel")
    void testDownloadUsers() {
        byte[] excelBytes = new byte[]{1, 2, 3};
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(testUserPage);
        when(downloadFileService.generateUserExcel(anyList())).thenReturn(ResponseEntity.ok(excelBytes));

        ResponseEntity<byte[]> result = userController.downloadUsers(0, 50, "createdAt", "asc");
        assertArrayEquals(excelBytes, result.getBody());
    }
}
