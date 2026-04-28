package com.user.api_users.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String gerar(String cpf, String nome, String cargo) {
        return Jwts.builder()
                .subject(cpf)
                .claim("nome", nome)
                .claim("cargo", cargo)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(chaveSecreta())
                .compact();
    }

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