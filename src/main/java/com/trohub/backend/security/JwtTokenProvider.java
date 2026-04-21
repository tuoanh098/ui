package com.trohub.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Long validityInMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") Long validityInMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMs = validityInMs;
    }

    public Long getValidityInMs() {
        return validityInMs;
    }

    public String generateToken(UserDetails userDetails, Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream().map(Object::toString).collect(Collectors.toList()));
        claims.put("userId", userId);

        return Jwts.builder()
                // setClaims replaces the entire body, so set it before subject OR set subject afterwards
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Authentication getAuthentication(String token, UserDetails userDetails) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        Object rolesObj = claims.get("roles");
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (rolesObj instanceof Collection) {
            Collection<?> coll = (Collection<?>) rolesObj;
            authorities = coll.stream().map(Object::toString).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }
}


