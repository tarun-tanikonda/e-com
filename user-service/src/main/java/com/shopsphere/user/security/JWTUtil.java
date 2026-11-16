package com.shopsphere.user.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.shopsphere.user.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for creating and verifying JWT access tokens using JJWT 0.12.x.
 * Injects user identity claims (userId, role, email) matching API Gateway expectations.
 */
@Component
public class JWTUtil {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours
    private final SecretKey key;

    public JWTUtil(@Value("${jwt.secret:${security.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}}") String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT access token for an authenticated User entity.
     * Injects userId, role, and email claims so API Gateway can propagate them downstream.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", "ROLE_" + user.getRole().name())
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * Generates a signed JWT access token for a UserDetails principal (fallback).
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority())
                        .orElse("ROLE_CUSTOMER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public boolean isTokenValid(String username, UserDetails userDetails, String token) {
        String tokenUsername = extractUsername(token);
        return tokenUsername != null
                && tokenUsername.equals(username)
                && tokenUsername.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public long getExpirationTime() {
        return EXPIRATION_TIME;
    }
}
