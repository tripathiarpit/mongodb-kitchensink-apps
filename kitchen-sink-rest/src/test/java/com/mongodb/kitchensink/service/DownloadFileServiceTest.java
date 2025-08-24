package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.ProfileDto;
import com.mongodb.kitchensink.dto.UserDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DownloadFileService.
 * This class verifies the functionality of generating Excel files
 * containing user data.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadFileService Tests")
class DownloadFileServiceTest {

    private DownloadFileService downloadFileService;

    @BeforeEach
    void setUp() {
        downloadFileService = new DownloadFileService();
    }

    /**
     * Helper method to get cell string value safely.
     * This method is made more robust to handle various cell types and
     * explicitly convert them to String, preventing IllegalStateException.
     * @param row The row to get the cell from.
     * @param cellNum The cell number.
     * @return The string value of the cell, or an empty string if null.
     */
    private String getCellStringValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) {
            return "";
        }

        // Explicitly convert all cell types to string based on their content
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // For numeric cells, including dates, convert to string.
                // Specific date formatting logic would go here if needed.
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Attempt to get the cached result of the formula as a string.
                // Fallback to the formula string itself if result is not directly convertible.
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            case ERROR:
                return "ERROR: " + cell.getErrorCellValue();
            default:
                // Fallback for any other unexpected or unhandled types
                return cell.toString(); // Provides a generic string representation
        }
    }

    /**
     * Tests successful generation of an Excel file with multiple users.
     * Verifies HTTP status, headers, and basic content of the Excel file,
     * including specific cell values for multiple rows.
     */
    @Test
    @DisplayName("Generate Excel with multiple users should succeed and verify content")
    void generateUserExcel_withMultipleUsers_shouldSucceed() throws IOException {
        // Given
        UserDto user1 = new UserDto(
                UUID.randomUUID().toString(),
                "user1@example.com",
                "user1_username",
                List.of("ADMIN", "USER"),
                true,
                Instant.now(),
                false,
                true, // isFirstLogin
                "secret1",    // twoFactorSecret
                true,         // twoFactorEnabled
                new ProfileDto(
                        "John",
                        "Doe",
                        "1234567890",
                        "123 Main St",
                        "Anytown",
                        "CA",
                        "USA",
                        "12345"
                )
        );

        UserDto user2 = new UserDto(
                UUID.randomUUID().toString(),
                "user2@example.com",
                "user2_username",
                List.of("USER"),
                false,
                Instant.now(),
                true,
                false, // isFirstLogin
                null,    // twoFactorSecret
                false,   // twoFactorEnabled
                new ProfileDto(
                        "Jane",
                        "Smith",
                        "0987654321",
                        "456 Oak Ave",
                        "Otherville",
                        "NY",
                        "USA",
                        "67890"
                )
        );
        List<UserDto> users = List.of(user1, user2);

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), response.getHeaders().getContentType());
        assertEquals("attachment; filename=users.xlsx", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertNotNull(response.getBody());

        // Verify content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            assertEquals(3, sheet.getPhysicalNumberOfRows(), "Sheet should have 1 header row + 2 data rows.");

            // Verify all headers in order
            Row headerRow = sheet.getRow(0);
            assertAll("Header Row Verification",
                    () -> assertEquals("Id", getCellStringValue(headerRow, 0)),
                    () -> assertEquals("Email", getCellStringValue(headerRow, 1)),
                    () -> assertEquals("Username", getCellStringValue(headerRow, 2)),
                    () -> assertEquals("Roles", getCellStringValue(headerRow, 3)),
                    () -> assertEquals("Active", getCellStringValue(headerRow, 4)),
                    () -> assertEquals("CreatedAt", getCellStringValue(headerRow, 5)),
                    () -> assertEquals("AccountVerificationPending", getCellStringValue(headerRow, 6)),
                    () -> assertEquals("FirstLogin", getCellStringValue(headerRow, 7)),
                    () -> assertEquals("FirstName", getCellStringValue(headerRow, 8)),
                    () -> assertEquals("LastName", getCellStringValue(headerRow, 9)),
                    () -> assertEquals("PhoneNumber", getCellStringValue(headerRow, 10)), // Corrected Profile fields start here
                    () -> assertEquals("Street", getCellStringValue(headerRow, 11)),
                    () -> assertEquals("City", getCellStringValue(headerRow, 12)),
                    () -> assertEquals("State", getCellStringValue(headerRow, 13)),
                    () -> assertEquals("Country", getCellStringValue(headerRow, 14)),
                    () -> assertEquals("Pincode", getCellStringValue(headerRow, 15)),
                    () -> assertEquals(16, headerRow.getPhysicalNumberOfCells(), "Expected 16 headers in total")
            );

            // Verify user1 data row
            Row dataRow1 = sheet.getRow(1);
            assertNotNull(dataRow1);
            assertEquals(user1.getId(), getCellStringValue(dataRow1, 0));
            assertEquals(user1.getEmail(), getCellStringValue(dataRow1, 1));
            assertEquals(user1.getUsername(), getCellStringValue(dataRow1, 2));
            assertEquals(String.join(";", user1.getRoles()), getCellStringValue(dataRow1, 3));
            assertEquals(String.valueOf(user1.isActive()), getCellStringValue(dataRow1, 4));
            // Verify CreatedAt string representation (assuming default Instant.toString() outputs in ISO 8601 UTC)
            assertTrue(getCellStringValue(dataRow1, 5).startsWith(user1.getCreatedAt().toString().substring(0, 10)), "CreatedAt should match date part");
            assertEquals(String.valueOf(user1.getAccountVerificationPending()), getCellStringValue(dataRow1, 6));
            assertEquals(String.valueOf(user1.getFirstLogin()), getCellStringValue(dataRow1, 7));
            assertEquals(user1.getProfile().getFirstName(), getCellStringValue(dataRow1, 8)); // FirstName
            assertEquals(user1.getProfile().getLastName(), getCellStringValue(dataRow1, 9)); // LastName
            assertEquals(user1.getProfile().getPhoneNumber(), getCellStringValue(dataRow1, 10)); // PhoneNumber
            assertEquals(user1.getProfile().getStreet(), getCellStringValue(dataRow1, 11)); // Address
            assertEquals(user1.getProfile().getCity(), getCellStringValue(dataRow1, 12)); // City
            assertEquals(user1.getProfile().getState(), getCellStringValue(dataRow1, 13)); // State
            assertEquals(user1.getProfile().getCountry(), getCellStringValue(dataRow1, 14)); // Country
            assertEquals(user1.getProfile().getPincode(), getCellStringValue(dataRow1, 15)); // ZipCode


            // Verify user2 data row
            Row dataRow2 = sheet.getRow(2);
            assertNotNull(dataRow2);
            assertEquals(user2.getId(), getCellStringValue(dataRow2, 0));
            assertEquals(user2.getEmail(), getCellStringValue(dataRow2, 1));
            assertEquals(user2.getUsername(), getCellStringValue(dataRow2, 2));
            assertEquals(String.join(";", user2.getRoles()), getCellStringValue(dataRow2, 3));
            assertEquals(String.valueOf(user2.isActive()), getCellStringValue(dataRow2, 4));
            // CreatedAt
            assertTrue(getCellStringValue(dataRow2, 5).startsWith(user2.getCreatedAt().toString().substring(0, 10)), "CreatedAt should match date part");
            assertEquals(String.valueOf(user2.getAccountVerificationPending()), getCellStringValue(dataRow2, 6));
            assertEquals(String.valueOf(user2.getFirstLogin()), getCellStringValue(dataRow2, 7));
            assertEquals(user2.getProfile().getFirstName(), getCellStringValue(dataRow2, 8)); // FirstName
            assertEquals(user2.getProfile().getLastName(), getCellStringValue(dataRow2, 9)); // LastName
            assertEquals(user2.getProfile().getPhoneNumber(), getCellStringValue(dataRow2, 10)); // PhoneNumber
            assertEquals(user2.getProfile().getStreet(), getCellStringValue(dataRow2, 11)); // Address
            assertEquals(user2.getProfile().getCity(), getCellStringValue(dataRow2, 12)); // City
            assertEquals(user2.getProfile().getState(), getCellStringValue(dataRow2, 13)); // State
            assertEquals(user2.getProfile().getCountry(), getCellStringValue(dataRow2, 14)); // Country
            assertEquals(user2.getProfile().getPincode(), getCellStringValue(dataRow2, 15)); // ZipCode
        }
    }

    /**
     * Tests that generating an Excel file with an empty user list
     * results in a file containing only headers.
     */
    @Test
    @DisplayName("Generate Excel with empty user list should create file with headers only")
    void generateUserExcel_withEmptyUserList_shouldCreateFileWithHeadersOnly() throws IOException {
        // Given
        List<UserDto> users = Collections.emptyList();

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), response.getHeaders().getContentType());
        assertEquals("attachment; filename=users.xlsx", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertNotNull(response.getBody());

        // Verify content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            assertEquals(1, sheet.getPhysicalNumberOfRows(), "The sheet should only have a header row.");
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("Id", getCellStringValue(headerRow, 0));
        }
    }

    /**
     * Tests that generating an Excel file for a user with a null profile
     * handles gracefully, writing empty strings for profile-related fields.
     */
    @Test
    @DisplayName("Generate Excel for user with null profile should handle gracefully and verify content")
    void generateUserExcel_withUserWithNullProfile_shouldHandleGracefully() throws IOException {
        // Given
        UserDto user = new UserDto(
                UUID.randomUUID().toString(),
                "nullprofile@example.com",
                "nullprofile_username",
                List.of("USER"),
                true,
                Instant.now(),
                false,
                false, // isFirstLogin
                null,    // twoFactorSecret
                false,   // twoFactorEnabled
                null // Null ProfileDto
        );
        List<UserDto> users = List.of(user);

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), response.getHeaders().getContentType());
        assertEquals("attachment; filename=users.xlsx", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertNotNull(response.getBody());

        // Verify content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            assertEquals(2, sheet.getPhysicalNumberOfRows(), "Sheet should have 1 header row + 1 data row.");
            Row dataRow = sheet.getRow(1); // The data row
            assertNotNull(dataRow);

            // Verify user data that is present
            assertEquals(user.getId(), getCellStringValue(dataRow, 0));
            assertEquals(user.getEmail(), getCellStringValue(dataRow, 1));
            assertEquals(user.getUsername(), getCellStringValue(dataRow, 2));

            // Verify profile-related cells are empty, now starting at index 10
            assertEquals("", getCellStringValue(dataRow, 10));  // FirstName
            assertEquals("", getCellStringValue(dataRow, 11)); // LastName
            assertEquals("", getCellStringValue(dataRow, 12)); // PhoneNumber
            assertEquals("", getCellStringValue(dataRow, 13)); // Address
            assertEquals("", getCellStringValue(dataRow, 14)); // City
            assertEquals("", getCellStringValue(dataRow, 15)); // State
            assertEquals("", getCellStringValue(dataRow, 16)); // Country
            assertEquals("", getCellStringValue(dataRow, 17)); // ZipCode
        }
    }

    /**
     * Tests generation with a single user, verifying basic content.
     */
    @Test
    @DisplayName("Generate Excel with a single user should succeed and verify content")
    void generateUserExcel_withSingleUser_shouldSucceed() throws IOException {
        // Given
        UserDto user = new UserDto(
                UUID.randomUUID().toString(),
                "singleuser@example.com",
                "single_username",
                List.of("USER", "EDITOR"),
                true,
                Instant.now(),
                false,
                true, // isFirstLogin
                "onlyones",
                true,
                new ProfileDto(
                        "Solo",
                        "User",
                        "1112223333",
                        "789 Pine Ln",
                        "Singleville",
                        "TX",
                        "USA",
                        "98765"
                )
        );
        List<UserDto> users = List.of(user);

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            assertEquals(2, sheet.getPhysicalNumberOfRows(), "Sheet should have 1 header row + 1 data row.");

            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals(user.getId(), getCellStringValue(dataRow, 0));
            assertEquals(user.getEmail(), getCellStringValue(dataRow, 1));
            assertEquals(user.getUsername(), getCellStringValue(dataRow, 2));
            assertEquals(String.join(";", user.getRoles()), getCellStringValue(dataRow, 3));
            assertEquals(String.valueOf(user.isActive()), getCellStringValue(dataRow, 4));
            // CreatedAt
            assertTrue(getCellStringValue(dataRow, 5).startsWith(user.getCreatedAt().toString().substring(0, 10)), "CreatedAt should match date part");
            assertEquals(String.valueOf(user.getAccountVerificationPending()), getCellStringValue(dataRow, 6));
            assertEquals(String.valueOf(user.getFirstLogin()), getCellStringValue(dataRow, 7));
            assertEquals(user.getTwoFactorSecret(), "onlyones");
            assertEquals(String.valueOf(user.getProfile().getLastName()), getCellStringValue(dataRow, 9));
            assertEquals(user.getProfile().getPhoneNumber(), getCellStringValue(dataRow, 10)); // Corrected index for FirstName
        }
    }

    /**
     * Tests generation for a user with an empty roles list.
     * Verifies that the roles cell is empty.
     */
    @Test
    @DisplayName("Generate Excel for user with empty roles list should handle gracefully")
    void generateUserExcel_withUserWithEmptyRoles_shouldHandleGracefully() throws IOException {
        // Given
        UserDto user = new UserDto(
                UUID.randomUUID().toString(),
                "emptyroles@example.com",
                "empty_roles_user",
                Collections.emptyList(), // Empty roles list
                true,
                Instant.now(),
                false,
                false, // isFirstLogin
                null,    // twoFactorSecret
                false,   // twoFactorEnabled
                new ProfileDto(
                        "No",
                        "Roles",
                        "0000000000",
                        "", "", "", "", ""
                )
        );
        List<UserDto> users = List.of(user);

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("", getCellStringValue(dataRow, 3)); // Roles cell should be empty
        }
    }

    /**
     * Tests generation for a user where the roles list itself is null.
     * Verifies that the roles cell is empty. This assumes the UserDto
     * constructor or builder allows a null roles list.
     */
    @Test
    @DisplayName("Generate Excel for user with null roles list should handle gracefully")
    void generateUserExcel_withUserWithNullRoles_shouldHandleGracefully() throws IOException {
        // Given
        UserDto user = new UserDto(
                UUID.randomUUID().toString(),
                "nullroles@example.com",
                "null_roles_user",
                null, // Null roles list
                true,
                Instant.now(),
                false,
                false, // isFirstLogin
                null,    // twoFactorSecret
                false,   // twoFactorEnabled
                new ProfileDto(
                        "Null",
                        "Roles",
                        "0000000000",
                        "", "", "", "", ""
                )
        );
        List<UserDto> users = List.of(user);

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("", getCellStringValue(dataRow, 3)); // Roles cell should be empty
        }
    }

    /**
     * Tests generation with a large number of users to check for performance or memory issues.
     * This is a basic stress test.
     */
    @Test
    @DisplayName("Generate Excel with a large number of users should succeed")
    void generateUserExcel_withLargeNumberOfUsers_shouldSucceed() throws IOException {
        // Given
        int numberOfUsers = 1000;
        List<UserDto> largeUserList = new java.util.ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            largeUserList.add(new UserDto(
                    UUID.randomUUID().toString(),
                    "user" + i + "@example.com",
                    "username" + i,
                    List.of("USER"),
                    true,
                    Instant.now(),
                    false,
                    false, // isFirstLogin
                    null,    // twoFactorSecret
                    false,   // twoFactorEnabled
                    new ProfileDto("Test", "User", "123", "Addr", "City", "State", "Country", "Zip")
            ));
        }

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(largeUserList);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            assertEquals(numberOfUsers + 1, sheet.getPhysicalNumberOfRows(), "Sheet should have header + all user rows.");
        }
    }

    /**
     * New Test Case: Verify handling of user data with various empty string fields.
     * Ensures that null or empty string fields in UserDto and ProfileDto are written as empty cells.
     */
    @Test
    @DisplayName("Generate Excel with user having empty string fields should handle gracefully")
    void generateUserExcel_withUserHavingEmptyStringFields_shouldHandleGracefully() throws IOException {
        // Given
        UserDto user = new UserDto(
                UUID.randomUUID().toString(),
                "", // Empty email
                "", // Empty username
                Collections.emptyList(), // Empty roles
                false,
                Instant.now(),
                true,
                false, // isFirstLogin
                "", // Empty twoFactorSecret
                false, // twoFactorEnabled
                new ProfileDto(
                        "", // Empty FirstName
                        "", // Empty LastName
                        "", // Empty PhoneNumber
                        "", // Empty Address
                        "", // Empty City
                        "", // Empty State
                        "", // Empty Country
                        "" // Empty ZipCode
                )
        );
        List<UserDto> users = List.of(user);

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            Sheet sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            assertEquals(2, sheet.getPhysicalNumberOfRows(), "Sheet should have 1 header row + 1 data row.");
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);

            assertEquals(user.getId(), getCellStringValue(dataRow, 0));
            assertEquals("", getCellStringValue(dataRow, 1)); // Email should be empty
            assertEquals("", getCellStringValue(dataRow, 2)); // Username should be empty
            assertEquals("", getCellStringValue(dataRow, 3)); // Roles should be empty
            assertEquals(String.valueOf(user.isActive()), getCellStringValue(dataRow, 4));
            assertTrue(getCellStringValue(dataRow, 5).startsWith(user.getCreatedAt().toString().substring(0, 10)), "CreatedAt should match date part");
            assertEquals(String.valueOf(user.getAccountVerificationPending()), getCellStringValue(dataRow, 6));
            assertEquals(String.valueOf(user.getFirstLogin()), getCellStringValue(dataRow, 7));
            assertEquals("", getCellStringValue(dataRow, 8)); // twoFactorSecret should be empty
            assertEquals(user.isTwoFactorEnabled(), false);
            assertEquals("", getCellStringValue(dataRow, 10)); // FirstName should be empty
            assertEquals("", getCellStringValue(dataRow, 11)); // LastName should be empty
            assertEquals("", getCellStringValue(dataRow, 12)); // PhoneNumber should be empty
            assertEquals("", getCellStringValue(dataRow, 13)); // Address should be empty
            assertEquals("", getCellStringValue(dataRow, 14)); // City should be empty
            assertEquals("", getCellStringValue(dataRow, 15)); // State should be empty
            assertEquals("", getCellStringValue(dataRow, 16)); // Country should be empty
            assertEquals("", getCellStringValue(dataRow, 17)); // ZipCode should be empty
        }
    }
}
