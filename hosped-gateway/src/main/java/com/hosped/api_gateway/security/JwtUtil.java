package com.hosped.api_gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(chaveSecreta())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getCpf(String token) {
        return Jwts.parser()
                .verifyWith(chaveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getCargo(String token) {
        return Jwts.parser()
                .verifyWith(chaveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("cargo", String.class);
    }

    private SecretKey chaveSecreta() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}