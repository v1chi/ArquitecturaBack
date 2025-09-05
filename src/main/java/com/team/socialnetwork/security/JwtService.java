package com.team.socialnetwork.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key signingKey;
    private final int accessMinutes;
    private final int emailTokenMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.accessToken.expirationMinutes}") int accessMinutes,
            @Value("${jwt.emailToken.expirationMinutes:1440}") int emailTokenMinutes
    ) {
        if (secret == null || secret.length() < 64) {
            throw new IllegalArgumentException("jwt.secret must be at least 64 characters long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMinutes = accessMinutes;
        this.emailTokenMinutes = emailTokenMinutes;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(claims)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateEmailToken(String subject) {
        Instant now = Instant.now();
        Instant expiry = now.plus(emailTokenMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("purpose", "email_confirm")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean isEmailToken(String token) {
        try {
            String purpose = extractClaim(token, claims -> claims.get("purpose", String.class));
            return "email_confirm".equals(purpose);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, String subject) {
        String tokenSubject = extractSubject(token);
        return subject.equals(tokenSubject) && !isTokenExpired(token);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}
