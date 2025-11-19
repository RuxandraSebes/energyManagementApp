// demoA/security/JwtUtil.java

package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // SECRET_KEY must be 32 characters or longer (256 bits) for HS256
    private static final String SECRET_KEY = "7579eed5ff630e842e0e8c48f22339167579eed5ff630e842e0e8c48f2233916"; // Changed to 64 chars
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // MODIFIED: Added Long userId parameter
    public String generateToken(String username, String role, Long userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId) // <--- NEW CLAIM
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Existing methods (for completeness, assumed they were already working)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // NEW: Extract userId
    public Long extractUserId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("userId", Long.class);
    }

    // NEW: Extract role
    public String extractRole(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("role", String.class);
    }
}