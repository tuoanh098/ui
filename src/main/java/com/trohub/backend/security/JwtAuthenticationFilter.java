package com.trohub.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (tokenProvider.validateToken(token)) {
                    String username = tokenProvider.getUsername(token);
                    if (username != null) {
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            var auth = (org.springframework.security.authentication.UsernamePasswordAuthenticationToken) tokenProvider.getAuthentication(token, userDetails);
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                            // If user not found, skip setting authentication. Request will be treated as unauthenticated.
                            logger.warn("User from JWT not found: {}", username);
                        }
                    } else {
                        logger.warn("JWT token does not contain a subject (username)");
                    }
                }
            } catch (Exception ex) {
                // Any exception during token parsing/validation should not stop the filter chain
                logger.warn("Could not set user authentication in security context", ex);
            }
        }

        filterChain.doFilter(request, response);
    }
}

