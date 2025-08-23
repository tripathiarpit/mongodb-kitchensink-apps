package com.mongodb.kitchensink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
/**
 * Class to Enabled Method based Security
 * <p>
 *It's primary purpose is enable and config method level security  example @PreAuthorize(hasRole('ADMIN')) in User Controller's method
 * </p>
 * @author Arpit Tripathi
 * @version 1.0
 * @since 2025-08-17
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
}