package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.service.DownloadFileService;
import com.mongodb.kitchensink.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private DownloadFileService downloadFileService;

    @InjectMocks
    private UserController userController;

    private UserDto userDto;
    private RegistrationRequest registrationRequest;
    private Page<UserDto> userDtoPage;
    private final String TEST_ID = "12345";
    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setEmail(TEST_EMAIL);
        userDto.setUsername("testuser");

        registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail(TEST_EMAIL);

        List<UserDto> userList = Arrays.asList(userDto, new UserDto());
        userDtoPage = new PageImpl<>(userList);
    }

    // --- registerUser Endpoint Tests ---

    @Test
    @DisplayName("registerUser should return OK when registration is successful")
    void registerUser_shouldReturnOk_onSuccess() {
        // Given
        RegistrationResponse response = new RegistrationResponse(true, "Registration successful");
        Authentication authentication = mock(Authentication.class);
        when(userService.registerUser(any(RegistrationRequest.class), any(Authentication.class))).thenReturn(response);

        // When
        ResponseEntity<RegistrationResponse> result = userController.registerUser(registrationRequest, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        verify(userService, times(1)).registerUser(any(RegistrationRequest.class), any(Authentication.class));
    }

    // --- getAllUsers Endpoint Tests ---

    @Test
    @DisplayName("getAllUsers should return paginated list of users")
    void getAllUsers_shouldReturnUsersPage() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "createdAt";
        String direction = "asc";

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userDtoPage);

        // When
        ResponseEntity<Page<UserDto>> result = userController.getAllUsers(page, size, sortBy, direction);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(userDtoPage.getContent().size(), result.getBody().getContent().size());
        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    @DisplayName("getAllUsers should handle descending sort direction")
    void getAllUsers_shouldHandleDescSort() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "createdAt";
        String direction = "desc";

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userDtoPage);

        // When
        ResponseEntity<Page<UserDto>> result = userController.getAllUsers(page, size, sortBy, direction);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).getAllUsers(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedPageable.getSort().getOrderFor(sortBy).getDirection());
    }

    // --- getUserById Endpoint Tests ---

    @Test
    @DisplayName("getUserById should return user when found")
    void getUserById_shouldReturnUser_whenFound() {
        // Given
        when(userService.getUserById(anyString())).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> result = userController.getUserById(TEST_ID);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(TEST_EMAIL, result.getBody().getEmail());
        verify(userService, times(1)).getUserById(TEST_ID);
    }

    // --- getUserByEmail Endpoint Tests ---

    @Test
    @DisplayName("getUserByEmail should return user when found")
    void getUserByEmail_shouldReturnUser_whenFound() {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> result = userController.getUserByEmail(TEST_EMAIL);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(TEST_EMAIL, result.getBody().getEmail());
        verify(userService, times(1)).getUserByEmail(TEST_EMAIL);
    }

    // --- getUsersByCity Endpoint Tests ---

    @Test
    @DisplayName("getUsersByCity should return paginated list of users by city")
    void getUsersByCity_shouldReturnUsersPage() {
        // Given
        String city = "New York";
        int page = 0;
        int size = 10;

        when(userService.getUsersByCity(anyString(), any(Pageable.class))).thenReturn(userDtoPage);

        // When
        ResponseEntity<Page<UserDto>> result = userController.getUsersByCity(city, page, size);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(userDtoPage.getContent().size(), result.getBody().getContent().size());
        verify(userService, times(1)).getUsersByCity(city, PageRequest.of(page, size));
    }

    // --- deleteUserByEmail Endpoint Tests ---

    @Test
    @DisplayName("deleteUserByEmail should return OK on successful deletion")
    void deleteUserByEmail_shouldReturnOk_onSuccess() {
        // Given
        ResourceDeleteResponse response = new ResourceDeleteResponse(true, "User deleted successfully");
        when(userService.deleteUserByEmail(anyString())).thenReturn(response);
        Map<String, String> requestBody = Map.of("email", TEST_EMAIL);

        // When
        ResponseEntity<ResourceDeleteResponse> result = userController.deleteUserByEmail(requestBody);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        verify(userService, times(1)).deleteUserByEmail(TEST_EMAIL);
    }

    // --- getUserByName Endpoint Tests ---

    @Test
    @DisplayName("getUserByName should return paginated list of users")
    void getUserByName_shouldReturnUsersPage() {
        // Given
        String name = "Test";
        when(userService.getUsersByName(anyString(), any(Pageable.class))).thenReturn(userDtoPage);

        // When
        ResponseEntity<Page<UserDto>> result = userController.getUserByName(name, 0, 50, "createdAt", "asc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(userDtoPage.getContent().size(), result.getBody().getContent().size());
        verify(userService, times(1)).getUsersByName(anyString(), any(Pageable.class));
    }

    // --- getUserByCity (paginated and sorted) Endpoint Tests ---

    @Test
    @DisplayName("getUserByCity should return paginated list of users with sorting")
    void getUserByCity_shouldReturnUsersPageWithSorting() {
        // Given
        String city = "New York";
        when(userService.getUsersByCity(anyString(), any(Pageable.class))).thenReturn(userDtoPage);

        // When
        ResponseEntity<Page<UserDto>> result = userController.getUserByCity(city, 0, 50, "createdAt", "asc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(userDtoPage.getContent().size(), result.getBody().getContent().size());
        verify(userService, times(1)).getUsersByCity(anyString(), any(Pageable.class));
    }

    // --- getUserByEmail (paginated and sorted) Endpoint Tests ---

    @Test
    @DisplayName("getUserByEmail should return paginated list of users with sorting")
    void getUserByEmail_shouldReturnUsersPageWithSorting() {
        // Given
        String email = "test@example.com";
        when(userService.getUsersByEmail(anyString(), any(Pageable.class))).thenReturn(userDtoPage);

        // When
        ResponseEntity<Page<UserDto>> result = userController.getUserByEmail(email, 0, 50, "createdAt", "asc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(userDtoPage.getContent().size(), result.getBody().getContent().size());
        verify(userService, times(1)).getUsersByEmail(anyString(), any(Pageable.class));
    }

    // --- getUserByCountry (paginated and sorted) Endpoint Tests ---

    @Test
    @DisplayName("getUserByCountry should return paginated list of users with sorting")
    void getUserByCountry_shouldReturnUsersPageWithSorting() {
        // Given
        String country = "USA";
        when(userService.getUsersByCountry(anyString(), any(Pageable.class))).thenReturn(userDtoPage);

        // When
        ResponseEntity<Page<UserDto>> result = userController.getUserByCountry(country, 0, 50, "createdAt", "asc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(userDtoPage.getContent().size(), result.getBody().getContent().size());
        verify(userService, times(1)).getUsersByCountry(anyString(), any(Pageable.class));
    }

    // --- updateUser Endpoint Tests ---

    @Test
    @DisplayName("updateUser should return updated UserDto on success")
    void updateUser_shouldReturnUpdatedUserDto_onSuccess() {
        // Given
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setEmail(TEST_EMAIL);
        updatedUserDto.setUsername("updateduser");
        when(userService.updateUser(anyString(), any(UserDto.class))).thenReturn(updatedUserDto);

        // When
        ResponseEntity<UserDto> result = userController.updateUser(TEST_EMAIL, updatedUserDto);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("updateduser", result.getBody().getUsername());
        verify(userService, times(1)).updateUser(TEST_EMAIL, updatedUserDto);
    }

    // --- updateUserProfile Endpoint Tests ---

    @Test
    @DisplayName("updateUserProfile should return updated UserDto on success")
    void updateUserProfile_shouldReturnUpdatedUserDto_onSuccess() {
        // Given
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setEmail(TEST_EMAIL);
        updatedUserDto.setUsername("updatedprofile");
        when(userService.updateUser(anyString(), any(UserDto.class))).thenReturn(updatedUserDto);

        // When
        ResponseEntity<UserDto> result = userController.updateUserProfile(TEST_EMAIL, updatedUserDto);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("updatedprofile", result.getBody().getUsername());
        verify(userService, times(1)).updateUser(TEST_EMAIL, updatedUserDto);
    }

    // --- downloadUsers Endpoint Tests ---

    @Test
    @DisplayName("downloadUsers should return byte array for a valid request")
    void downloadUsers_shouldReturnByteArray_onSuccess() {
        // Given
        byte[] excelBytes = "excel_data".getBytes();
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userDtoPage);
        when(downloadFileService.generateUserExcel(anyList())).thenReturn(ResponseEntity.ok(excelBytes));

        // When
        ResponseEntity<byte[]> result = userController.downloadUsers(0, 50, "createdAt", "asc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(excelBytes, result.getBody());
        verify(userService, times(1)).getAllUsers(any(Pageable.class));
        verify(downloadFileService, times(1)).generateUserExcel(anyList());
    }

    // --- downloadUsersbyEmails Endpoint Tests ---

    @Test
    @DisplayName("downloadUsersbyEmails should return byte array for a list of emails")
    void downloadUsersbyEmails_shouldReturnByteArray_onSuccess() {
        // Given
        byte[] excelBytes = "excel_data".getBytes();
        ArrayList<String> emailIds = new ArrayList<>(Arrays.asList("test1@example.com", "test2@example.com"));

        // Correct stubbing: Use a type-safe matcher
        // Mockito.any(ArrayList.class) is the correct way to specify the type.
        when(userService.getAllUsersByEmailIds(any(ArrayList.class))).thenReturn(Arrays.asList(new UserDto(), new UserDto()));

        when(downloadFileService.generateUserExcel(anyList())).thenReturn(ResponseEntity.ok(excelBytes));

        // When
        ResponseEntity<byte[]> result = userController.downloadUsersbyEmails(emailIds);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(excelBytes, result.getBody());

        // Use an ArgumentCaptor to verify the specific list passed to the service
        ArgumentCaptor<ArrayList> arrayListCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(userService, times(1)).getAllUsersByEmailIds(arrayListCaptor.capture());

        // Assert that the captured list is what you expect
        assertEquals(emailIds, arrayListCaptor.getValue());

        verify(downloadFileService, times(1)).generateUserExcel(anyList());
    }
}