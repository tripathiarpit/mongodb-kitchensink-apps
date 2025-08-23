package com.mongodb.kitchensink.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Class to Configure OpenAPI3.0 for MongoDB kitchen sink APP
 * <p>
 *It's primary purpose is to allow user of Swagger to attach the JWT token in Authorization Header
 * </p>
 * @author Arpit Tripathi
 * @version 1.0
 * @since 2025-08-17
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kitchensinkOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
                .info(new Info()
                        .title("MongoDB kitchensink API")
                        .version("1.0")
                        .description("API documentation for MongoDB Kitchensink application"));
    }
}
