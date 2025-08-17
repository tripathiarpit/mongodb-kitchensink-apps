package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.service.DownloadFileService;
import com.mongodb.kitchensink.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * Controller for managing user-related operations.
 * <p>
 * This includes registration, retrieval, search, update, and deletion of users.
 * All endpoints support JSON request/response.
 * </p>
 *
 * @author Arpit Tripathi
 * @version 1.0
 * @since 2025-08-17
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Operations related to user registration, retrieval, and management")
public class UserController {

    private final UserService userService;
    private final DownloadFileService downloadFileService;

    @Autowired
    public UserController(UserService userService, DownloadFileService downloadFileService) {
        this.userService = userService;
        this.downloadFileService = downloadFileService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and profile. Public users are assigned the USER role by default; Admins can assign multiple roles.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegistrationRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "John Doe",
                                      "email": "john@example.com",
                                      "password": "P@ssw0rd",
                                      "phoneNumber": "9876543210",
                                      "address": {
                                        "street": "123 Main Street",
                                        "city": "New York",
                                        "state": "NY",
                                        "pincode": "10001",
                                        "country": "USA"
                                      },
                                      "roles": ["USER"]
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registration successful"),
                    @ApiResponse(responseCode = "400", description = "Email already exists")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerUser(
            @Valid @RequestBody RegistrationRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userService.registerUser(request, authentication));
    }

    @Operation(
            summary = "Get all registered users (paginated)",
            description = "Admin-only endpoint to retrieve a paginated list of all registered users. Supports sorting by any user attribute.",
            parameters = {
                    @Parameter(name = "page", description = "Page number starting from 0", example = "0"),
                    @Parameter(name = "size", description = "Number of users per page", example = "50"),
                    @Parameter(name = "sortBy", description = "Field to sort the results by", example = "createdAt"),
                    @Parameter(name = "direction", description = "Sort direction (asc or desc)", example = "asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of users")
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @Operation(
            summary = "Get user by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Get user by email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(summary = "Search users by city (paginated)")
    @GetMapping("/city/{city}")
    public ResponseEntity<Page<UserDto>> getUsersByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getUsersByCity(city, pageable));
    }

    @Operation(summary = "Delete user by email (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete")
    public ResponseEntity<ResourceDeleteResponse> deleteUserByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return ResponseEntity.ok(userService.deleteUserByEmail(email));
    }

    @Operation(summary = "Search users by name (paginated)")
    @GetMapping("/getUserByName")
    public ResponseEntity<Page<UserDto>> getUserByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(userService.getUsersByName(name, pageable));
    }

    @Operation(summary = "Search users by city with pagination and sorting")
    @GetMapping("/getUserByCity")
    public ResponseEntity<Page<UserDto>> getUserByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(userService.getUsersByCity(city, pageable));
    }

    @Operation(summary = "Search users by email with pagination and sorting")
    @GetMapping("/getUserByEmail")
    public ResponseEntity<Page<UserDto>> getUserByEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(userService.getUsersByEmail(email, pageable));
    }

    @Operation(summary = "Search users by country with pagination and sorting")
    @GetMapping("/getUserByCountry")
    public ResponseEntity<Page<UserDto>> getUserByCountry(
            @RequestParam String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(userService.getUsersByCountry(country, pageable));
    }

    @Operation(summary = "Update user details by email/ID")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("id") String emailId,
            @RequestBody UserDto request
    ) {
        return ResponseEntity.ok(userService.updateUser(emailId, request));
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadUsers(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size,
                                                @RequestParam(defaultValue = "createdAt") String sortBy,
                                                @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Page<UserDto> pages = userService.getAllUsers(pageable);
        List<UserDto> users = pages.getContent();
        return downloadFileService.generateUserExcel(users);
    }


}
