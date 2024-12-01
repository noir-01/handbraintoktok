package com.example.handbrainserver.music.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.util.Date;

@Component
public class JwtUtil {
    

    private static String SECRET_KEY;
    @Value("${jwt.secretKey}")
    private String secret_key;

    @PostConstruct
    public void init() {
        this.SECRET_KEY = secret_key;
    }
    

    private long expirationTime = 15839520000L; // 180일

    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token, String phoneNum) {
        final String extractedUsername = extractUserId(token);
        return (extractedUsername.equals(phoneNum) && !isTokenExpired(token));
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
