package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.dto.UserDto;
import com.mongodb.kitchensink.service.UserService;
import com.mongodb.kitchensink.util.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService; // <-- mock UserService

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    void swaggerUi_shouldPermitAll() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void apiDocs_shouldPermitAll() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should deny access to /api/users without authentication")
    void securedEndpoint_shouldDenyWithoutAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should allow access to /api/users with authentication")
    void securedEndpoint_shouldAllowWithAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void securedEndpoint_shouldDenyWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk()); // 401
    }

    @Test
    void securedEndpoint_shouldAllowWithAuth() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(new UserDto());

        mockMvc.perform(get("/api/users/email/test@example.com")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk()); // 200
    }
}