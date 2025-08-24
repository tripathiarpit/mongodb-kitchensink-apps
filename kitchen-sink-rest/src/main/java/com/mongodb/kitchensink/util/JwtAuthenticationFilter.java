package com.mongodb.kitchensink.util;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.exception.AccountVerificationException;
import com.mongodb.kitchensink.exception.JwtExpiredException;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final com.mongodb.kitchensink.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RedisTemplate<String, Object> redisTemplate;
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserRepository userRepository,
                                   com.mongodb.kitchensink.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                                   RedisTemplate<String, Object> redisTemplate
                                   ) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String accessToken = null; // Use a more specific name like accessToken
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        try {
            if (accessToken != null) {
                tokenProvider.validateAccessToken(accessToken);
                String email = tokenProvider.getEmailFromAccessToken(accessToken);

                String storedAccessToken = (String) redisTemplate.opsForValue().get("ACTIVE_ACCESS_TOKEN:" + email);
                if (storedAccessToken == null || !storedAccessToken.equals(accessToken)) {
                    throw new JwtExpiredException(ErrorCodes.VALIDATION_ERROR, ErrorMessageConstants.TOKEN_EXPIRED);
                }
                User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found from token payload"));
                List<GrantedAuthority> authorities = currentUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                currentUser.getEmail(),
                                null,
                                authorities
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (AccountVerificationException accountVerificationExcpetion) {
            jwtAuthenticationEntryPoint.commence(request, response,
                    new org.springframework.security.core.AuthenticationException(accountVerificationExcpetion.getMessage()) {});
        }
        catch (JwtExpiredException jwtExpiredException) {
            jwtAuthenticationEntryPoint.commence(request, response,
                    new org.springframework.security.core.AuthenticationException(jwtExpiredException.getMessage()) {});
        }
        catch (Exception ex) {
            jwtAuthenticationEntryPoint.commence(request, response,
                    new org.springframework.security.core.AuthenticationException("JWT invalid or expired: " + ex.getMessage()) {});
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") || path.startsWith("/api/users/register") || path.startsWith("/api/auth/logout");
    }
}