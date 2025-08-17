package com.mongodb.kitchensink.util;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.exception.AccountVerificationExcpetion;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.UserRepository;
import com.mongodb.kitchensink.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.SessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final com.mongodb.kitchensink.util.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserRepository userRepository, SessionService sessionService,
                                   com.mongodb.kitchensink.util.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        try {
            if (token != null ) {
               tokenProvider.validateToken(token);
                String email = tokenProvider.getEmailFromToken(token);
                User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                System.out.println("User roles from DB: " + currentUser.getRoles());

                List<GrantedAuthority> authorities = currentUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                boolean sessionValid = sessionService.validateSessionToken(email, token);
                if (!sessionValid) {
                    throw new AccountVerificationExcpetion(ErrorCodes.SESSION_EXPIRED);
                }
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                currentUser.getEmail(),
                                null,
                                authorities
                        );
                System.out.println("Granted authorities: " + authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (AccountVerificationExcpetion accountVerificationExcpetion) {
            jwtAuthenticationEntryPoint.commence(request, response,
                    new org.springframework.security.core.AuthenticationException(accountVerificationExcpetion.getMessage()) {});
        }
        catch (Exception ex) {
            jwtAuthenticationEntryPoint.commence(request, response,
                    new org.springframework.security.core.AuthenticationException("JWT invalid or expired") {});
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") || path.startsWith("/api/users/register");
    }
}
