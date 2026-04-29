package com.hosped.api_gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenValidator {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(chaveSecreta())
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getCargo(String token) {
        return Jwts.parser()
                .verifyWith(chaveSecreta())
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("cargo", String.class);
    }

    private SecretKey chaveSecreta() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
