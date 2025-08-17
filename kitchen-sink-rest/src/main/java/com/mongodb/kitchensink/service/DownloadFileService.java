package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.ProfileDto;
import com.mongodb.kitchensink.dto.UserDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class DownloadFileService {

    // Header constants
    public static class UserExcelHeaders {
        public static final String[] HEADERS = {
                "Id", "Email", "Username", "Roles", "Active", "CreatedAt",
                "AccountVerificationPending", "FirstLogin", "FirstName", "LastName",
                "PhoneNumber", "Street", "City", "State", "Country", "Pincode"
        };
    }

    public ResponseEntity<byte[]> generateUserExcel(List<UserDto> users) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();

            // Font for headers
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14); // Bigger font size
            headerFont.setColor(IndexedColors.BLACK.getIndex()); // Black font
            headerStyle.setFont(headerFont);

            // Background color (light blue)
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Align center
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Apply header style
            for (int i = 0; i < UserExcelHeaders.HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(UserExcelHeaders.HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Adjust header row height
            headerRow.setHeightInPoints(20);

            // Fill data rows
            int rowIdx = 1;
            for (UserDto user : users) {
                ProfileDto profile = user.getProfile();
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(user.getId() != null ? user.getId().toString() : "");
                row.createCell(1).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(2).setCellValue(user.getUsername() != null ? user.getUsername() : "");
                row.createCell(3).setCellValue(user.getRoles() != null ? String.join(";", user.getRoles()) : "");
                row.createCell(4).setCellValue(user.isActive());
                row.createCell(5).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
                row.createCell(6).setCellValue(user.getAccountVerificationPending());
                row.createCell(7).setCellValue(user.getFirstLogin());
                row.createCell(8).setCellValue(profile != null ? profile.getFirstName() : "");
                row.createCell(9).setCellValue(profile != null ? profile.getLastName() : "");
                row.createCell(10).setCellValue(profile != null ? profile.getPhoneNumber() : "");
                row.createCell(11).setCellValue(profile != null ? profile.getStreet() : "");
                row.createCell(12).setCellValue(profile != null ? profile.getCity() : "");
                row.createCell(13).setCellValue(profile != null ? profile.getState() : "");
                row.createCell(14).setCellValue(profile != null ? profile.getCountry() : "");
                row.createCell(15).setCellValue(profile != null ? profile.getPincode() : "");
            }

            // Auto-size columns
            for (int i = 0; i < UserExcelHeaders.HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
}
