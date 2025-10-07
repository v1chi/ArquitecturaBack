package com.team.socialnetwork.security;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.MalformedJwtException;

@DisplayName("JWT Service Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "test-secret-key-for-tests-that-is-long-enough-for-hmac-sha256-algorithm";
    private final int accessMinutes = 60;
    private final int emailTokenMinutes = 1440;
    private final int passwordResetMinutes = 60;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(testSecret, accessMinutes, emailTokenMinutes, passwordResetMinutes);
    }

    @Test
    @DisplayName("Should throw exception when secret is too short")
    void shouldThrowExceptionWhenSecretTooShort() {
        String shortSecret = "short";
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JwtService(shortSecret, 60, 1440, 60)
        );
        
        assertEquals("jwt.secret must be at least 64 characters long", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when secret is null")
    void shouldThrowExceptionWhenSecretNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JwtService(null, 60, 1440, 60)
        );
        
        assertEquals("jwt.secret must be at least 64 characters long", exception.getMessage());
    }

    @Test
    @DisplayName("Should generate valid access token")
    void shouldGenerateValidAccessToken() {
        String subject = "test@example.com";
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("name", "Test User");

        String token = jwtService.generateAccessToken(subject, claims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Should extract subject from access token")
    void shouldExtractSubjectFromAccessToken() {
        String subject = "test@example.com";
        Map<String, Object> claims = new HashMap<>();
        
        String token = jwtService.generateAccessToken(subject, claims);
        String extractedSubject = jwtService.extractSubject(token);

        assertEquals(subject, extractedSubject);
    }

    @Test
    @DisplayName("Should generate valid email confirmation token")
    void shouldGenerateValidEmailToken() {
        String subject = "test@example.com";

        String token = jwtService.generateEmailToken(subject);

        assertNotNull(token);
        assertTrue(jwtService.isEmailToken(token));
        assertEquals(subject, jwtService.extractSubject(token));
    }

    @Test
    @DisplayName("Should identify email token correctly")
    void shouldIdentifyEmailTokenCorrectly() {
        String subject = "test@example.com";
        String emailToken = jwtService.generateEmailToken(subject);
        
        Map<String, Object> claims = new HashMap<>();
        String accessToken = jwtService.generateAccessToken(subject, claims);

        assertTrue(jwtService.isEmailToken(emailToken));
        assertFalse(jwtService.isEmailToken(accessToken));
    }

    @Test
    @DisplayName("Should generate valid password reset token")
    void shouldGenerateValidPasswordResetToken() {
        String subject = "test@example.com";

        String token = jwtService.generatePasswordResetToken(subject);

        assertNotNull(token);
        assertTrue(jwtService.isPasswordResetToken(token));
        assertEquals(subject, jwtService.extractSubject(token));
    }

    @Test
    @DisplayName("Should identify password reset token correctly")
    void shouldIdentifyPasswordResetTokenCorrectly() {
        String subject = "test@example.com";
        String passwordResetToken = jwtService.generatePasswordResetToken(subject);
        
        Map<String, Object> claims = new HashMap<>();
        String accessToken = jwtService.generateAccessToken(subject, claims);

        assertTrue(jwtService.isPasswordResetToken(passwordResetToken));
        assertFalse(jwtService.isPasswordResetToken(accessToken));
    }

    @Test
    @DisplayName("Should validate token correctly")
    void shouldValidateTokenCorrectly() {
        String subject = "test@example.com";
        Map<String, Object> claims = new HashMap<>();
        
        String token = jwtService.generateAccessToken(subject, claims);

        assertTrue(jwtService.isTokenValid(token, subject));
        assertFalse(jwtService.isTokenValid(token, "different@example.com"));
    }

    @Test
    @DisplayName("Should handle invalid token gracefully")
    void shouldHandleInvalidTokenGracefully() {
        String invalidToken = "invalid.token.here";

        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractSubject(invalidToken);
        });
    }

    @Test
    @DisplayName("Should return false for invalid email token")
    void shouldReturnFalseForInvalidEmailToken() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtService.isEmailToken(invalidToken));
    }

    @Test
    @DisplayName("Should return false for invalid password reset token")
    void shouldReturnFalseForInvalidPasswordResetToken() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtService.isPasswordResetToken(invalidToken));
    }

    @Test
    @DisplayName("Should extract custom claims from token")
    void shouldExtractCustomClaimsFromToken() {
        String subject = "test@example.com";
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123L);
        claims.put("name", "Test User");

        String token = jwtService.generateAccessToken(subject, claims);

        Long userId = jwtService.extractClaim(token, claimSet -> claimSet.get("userId", Long.class));
        String name = jwtService.extractClaim(token, claimSet -> claimSet.get("name", String.class));

        assertEquals(123L, userId);
        assertEquals("Test User", name);
    }
}