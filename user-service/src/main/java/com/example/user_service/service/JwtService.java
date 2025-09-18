package com.example.user_service.service;

import com.example.user_service.model.*;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import io.jsonwebtoken.Claims;
import javax.crypto.SecretKey;


@Service
public class JwtService {
    private static final String SECRET_KEY = "my-very-long-and-secure-jwt-secret-key-123456";

    public String generateTokenAccess(User user) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("userId", user.getId())
            .claim("type", "access")
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + 900000)) // 15 phút
            .signWith(key)
            .compact();
    }
    public String generateRefreshToken(User user){
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("userId",user.getId())
            .claim("type", "refresh")
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + 604800000)) // 7 ngày
            .signWith(key)
            .compact();
    }

    public Claims extractClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(String token) {
        return extractClaims(token).getExpiration().after(new Date());
    }

    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }
}