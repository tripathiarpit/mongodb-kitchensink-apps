package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.ProfileDto;
import com.mongodb.kitchensink.dto.UserDto;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadFileService Tests")
class DownloadFileServiceTest {

    private DownloadFileService downloadFileService;

    @BeforeEach
    void setUp() {
        downloadFileService = new DownloadFileService();
    }

    @Test
    @DisplayName("Generate Excel with multiple users should succeed")
    void generateUserExcel_withMultipleUsers_shouldSucceed() throws IOException {
        // Given

        List<UserDto> users = List.of(
                new UserDto(
                        UUID.randomUUID().toString(), // id needs to be a String
                        "user1@example.com",
                        "user1",
                        List.of("ADMIN", "USER"),
                        true,
                        Instant.now(),
                        false,
                        true,
                        null,    // twoFactorSecret
                        false,   // twoFactorEnabled
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
                ),
                new UserDto(
                        UUID.randomUUID().toString(), // id needs to be a String
                        "user2@example.com",
                        "user2",
                        List.of("USER"),
                        false,
                        Instant.now(),
                        true,
                        false,
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
                )
        );

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
            assertNotNull(workbook.getSheet("Users"));
            // Further verification of rows and cell values can be added here
        }
    }

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
        assertNotNull(response.getBody());

        // Verify content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            var sheet = workbook.getSheet("Users");
            assertNotNull(sheet);
            assertEquals(1, sheet.getPhysicalNumberOfRows(), "The sheet should only have a header row.");
        }
    }

    @Test
    @DisplayName("Generate Excel for user with null profile should handle gracefully")
    void generateUserExcel_withUserWithNullProfile_shouldHandleGracefully() throws IOException {
        // Given
        List<UserDto> users = List.of(
                new UserDto(
                        UUID.randomUUID().toString(), // id needs to be a String
                        "dsdsdsd@example.com",
                        "dsdsd",
                        List.of("ADMIN", "USER"),
                        true,
                        Instant.now(),
                        false,
                        true,
                        null,
                        false,
                        null
        ));

        // When
        ResponseEntity<byte[]> response = downloadFileService.generateUserExcel(users);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            var sheet = workbook.getSheet("Users");
            var dataRow = sheet.getRow(1); // The data row
            assertNotNull(dataRow);
            // Verify profile-related cells are empty
            assertEquals("", dataRow.getCell(8).getStringCellValue()); // FirstName
            assertEquals("", dataRow.getCell(9).getStringCellValue()); // LastName
            assertEquals("", dataRow.getCell(10).getStringCellValue()); // PhoneNumber
        }
    }
}