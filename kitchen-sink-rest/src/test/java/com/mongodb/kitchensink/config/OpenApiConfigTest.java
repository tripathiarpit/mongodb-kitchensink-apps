package com.mongodb.kitchensink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the OpenApiConfig class.
 * This class verifies that the OpenAPI bean is correctly configured with
 * the expected security schemes, information, and descriptions.
 */
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    /**
     * Set up the OpenApiConfig instance before each test.
     */
    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    /**
     * Tests that the kitchensinkOpenAPI method returns a non-null OpenAPI object.
     */
    @Test
    @DisplayName("should return a non-null OpenAPI object")
    void kitchensinkOpenAPI_shouldReturnNonNull() {
        OpenAPI openAPI = openApiConfig.kitchensinkOpenAPI();
        assertNotNull(openAPI, "OpenAPI object should not be null");
    }

    /**
     * Tests that the OpenAPI object contains the correct Info details.
     */
    @Test
    @DisplayName("should contain correct API info details")
    void kitchensinkOpenAPI_shouldHaveCorrectInfo() {
        OpenAPI openAPI = openApiConfig.kitchensinkOpenAPI();
        Info info = openAPI.getInfo();

        assertNotNull(info, "Info object should not be null");
        assertEquals("MongoDB kitchensink API", info.getTitle(), "API title should match");
        assertEquals("1.0", info.getVersion(), "API version should match");
        assertEquals("API documentation for MongoDB Kitchensink application", info.getDescription(), "API description should match");
    }

    /**
     * Tests that the OpenAPI object defines the 'bearer-key' security scheme.
     */
    @Test
    @DisplayName("should define 'bearer-key' security scheme")
    void kitchensinkOpenAPI_shouldDefineBearerKeySecurityScheme() {
        OpenAPI openAPI = openApiConfig.kitchensinkOpenAPI();
        assertNotNull(openAPI.getComponents(), "Components should not be null");
        assertNotNull(openAPI.getComponents().getSecuritySchemes(), "Security schemes map should not be null");

        SecurityScheme bearerKeyScheme = openAPI.getComponents().getSecuritySchemes().get("bearer-key");
        assertNotNull(bearerKeyScheme, "Bearer key security scheme should be defined");
        assertEquals(SecurityScheme.Type.HTTP, bearerKeyScheme.getType(), "Bearer key type should be HTTP");
        assertEquals("bearer", bearerKeyScheme.getScheme(), "Bearer key scheme should be 'bearer'");
        assertEquals("JWT", bearerKeyScheme.getBearerFormat(), "Bearer key format should be JWT");
    }

    /**
     * Tests that the OpenAPI object includes a security requirement for 'bearer-key'.
     */
    @Test
    @DisplayName("should include 'bearer-key' in security requirements")
    void kitchensinkOpenAPI_shouldIncludeBearerKeySecurityRequirement() {
        OpenAPI openAPI = openApiConfig.kitchensinkOpenAPI();
        assertNotNull(openAPI.getSecurity(), "Security requirements list should not be null");
        assertFalse(openAPI.getSecurity().isEmpty(), "Security requirements list should not be empty");

        boolean bearerKeyRequirementFound = openAPI.getSecurity().stream()
                .anyMatch(sr -> sr.containsKey("bearer-key")); // FIX: Removed .getRequirements()

        assertTrue(bearerKeyRequirementFound, "Security requirements should include 'bearer-key'");
    }

    /**
     * Tests the overall structure of the OpenAPI configuration.
     */
    @Test
    @DisplayName("should have a complete and valid OpenAPI configuration structure")
    void kitchensinkOpenAPI_shouldHaveValidStructure() {
        OpenAPI openAPI = openApiConfig.kitchensinkOpenAPI();

        // Comprehensive check: combines previous assertions into one structural test
        assertAll("OpenAPI configuration structure validation",
                () -> assertNotNull(openAPI.getInfo(), "Info should be present"),
                () -> assertEquals("MongoDB kitchensink API", openAPI.getInfo().getTitle()),
                () -> assertNotNull(openAPI.getComponents(), "Components should be present"),
                () -> assertNotNull(openAPI.getComponents().getSecuritySchemes(), "Security schemes should be present"),
                () -> {
                    SecurityScheme bearerKeyScheme = openAPI.getComponents().getSecuritySchemes().get("bearer-key");
                    assertNotNull(bearerKeyScheme, "Bearer key scheme should be defined");
                    assertEquals(SecurityScheme.Type.HTTP, bearerKeyScheme.getType());
                    assertEquals("bearer", bearerKeyScheme.getScheme());
                    assertEquals("JWT", bearerKeyScheme.getBearerFormat());
                },
                () -> assertNotNull(openAPI.getSecurity(), "Security requirements should be present"),
                () -> assertFalse(openAPI.getSecurity().isEmpty(), "Security requirements should not be empty"),
                () -> assertTrue(openAPI.getSecurity().stream()
                                .anyMatch(sr -> sr.containsKey("bearer-key")), // FIX: Removed .getRequirements()
                        "Security requirements should include 'bearer-key'")
        );
    }
}
