package com.shopsphere.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private JwtUtil jwtUtil;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
    }

    private String createToken(String subject, Map<String, Object> claims, long durationMs) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + durationMs))
                .signWith(signingKey)
                .compact();
    }

    @Test
    @DisplayName("Valid JWT token should pass validation and extract claims")
    void validTokenShouldPassValidation() {
        String token = createToken("user@shopsphere.com", Map.of("userId", "101", "role", "ROLE_CUSTOMER"), 3600_000);

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("101", jwtUtil.extractUserId(token));
        assertEquals("ROLE_CUSTOMER", jwtUtil.extractRole(token));
        assertEquals("user@shopsphere.com", jwtUtil.extractEmail(token));
    }

    @Test
    @DisplayName("Expired JWT token should fail validation")
    void expiredTokenShouldFailValidation() {
        // Expired 10 seconds ago
        String expiredToken = createToken("user@shopsphere.com", Map.of("userId", "101"), -10_000);

        assertFalse(jwtUtil.isTokenValid(expiredToken));
    }

    @Test
    @DisplayName("Tampered / invalid JWT token should fail validation")
    void tamperedTokenShouldFailValidation() {
        String token = createToken("user@shopsphere.com", Map.of("userId", "101"), 3600_000);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(jwtUtil.isTokenValid(tamperedToken));
    }
}
