package com.jobportal.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${app.jwt.access-secret}")
    private String accessSecret;

    @Value("${app.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey accessKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes());
    }

    private SecretKey refreshKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    // ---- Generation ----

    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(accessKey())
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(refreshKey())
                .compact();
    }

    // ---- Extraction ----

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject, accessKey());
    }

    public String extractEmailFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, refreshKey());
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token, accessKey());
        return claims.get("role", String.class);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver, SecretKey key) {
        Claims claims = extractAllClaims(token, key);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ---- Validation ----

    public boolean isAccessTokenValid(String token) {
        try {
            extractAllClaims(token, accessKey());
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            extractAllClaims(token, refreshKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
