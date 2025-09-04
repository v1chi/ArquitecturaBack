package com.ejemplo.mibackend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    // Clave secreta para firmar los tokens (en producción debe estar en variables de entorno)
    // DEBE tener al menos 64 caracteres (512 bits) para HS512
    private final String jwtSecret = "miClaveSecretaSuperSeguraParaJWTTokensDeAlMenos512BitsParaHS512AlgoritmoDeSeguridad";
    
    // Tiempo de expiración del token (24 horas)
    private final int jwtExpirationMs = 86400000; // 24 * 60 * 60 * 1000
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    // Generar token JWT
    public String generateToken(String email, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    // Obtener email del token
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    // Obtener userId del token
    public Long getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }
    
    // Validar token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token JWT inválido: " + e.getMessage());
            return false;
        }
    }
}
