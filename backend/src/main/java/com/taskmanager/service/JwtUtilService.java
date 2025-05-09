package com.taskmanager.service;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtilService {

    private final Key jwtSecretKey;
    private final long expirationTime;

    public JwtUtilService(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration}") long expirationTime) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.expirationTime = expirationTime;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecretKey)
                    .parseClaimsJws(token)
                    .getBody();
            String extractedUsername = claims.getSubject();
            Date expiration = claims.getExpiration();
            boolean isNotExpired = !expiration.before(new Date());
            boolean usernameMatches = extractedUsername.equals(username);
            return usernameMatches && isNotExpired;
        } catch (Exception e) {
            return false;
        }
    }
}