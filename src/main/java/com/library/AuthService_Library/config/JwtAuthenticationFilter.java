package com.library.AuthService_Library.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.library.AuthService_Library.util.JwtTokenUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService, HandlerExceptionResolver resolver) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extract the Token from the Authorization Header
            String jwt = parseJwt(request);

            // 2. If valid token found and user is not already authenticated in this thread context
            if (jwt != null) {
                String username = jwtTokenUtil.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 3. Load user details from your database service configuration
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    // 4. Construct the core Authentication Object
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                    );
                    
                    // 5. Build remote request details tracking (IP, session info, etc.)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Establish the security context for the thread lifecycle
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException ex) {
            // Log security parsing failures in production (e.g., expired token, malformed signature)
            logger.error("Cannot set user authentication: {}", ex);
            
            // Forward the exception directly to your RestControllerAdvice
            resolver.resolveException(request, response, null, ex);
        }

        // Forward execution to the remaining filters in the pipeline
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);

        // JWT tokens traditionally arrive within a "Bearer <token_string>" layout
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}

