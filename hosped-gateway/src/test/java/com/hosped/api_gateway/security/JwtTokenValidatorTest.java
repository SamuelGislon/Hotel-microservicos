package com.hosped.api_gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenValidatorTest {

    private static final String SECRET = "hosped@Secret#2025!xK9mPqL3nZvB8wR";

    private JwtTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JwtTokenValidator();
        ReflectionTestUtils.setField(validator, "secret", SECRET);
        ReflectionTestUtils.setField(validator, "issuer", "hosped-users");
    }

    @Test
    void validaTokenEmitidoPeloUsers() {
        String token = gerarToken("hosped-users", "ADMINISTRADOR");

        assertTrue(validator.isValid(token));
        assertEquals("ADMINISTRADOR", validator.getCargo(token));
    }

    @Test
    void rejeitaTokenComIssuerDiferente() {
        String token = gerarToken("outro-servico", "ADMINISTRADOR");

        assertFalse(validator.isValid(token));
    }

    private String gerarToken(String issuer, String cargo) {
        return Jwts.builder()
                .issuer(issuer)
                .subject("12345678900")
                .claim("id", "a6075738-f42d-45a4-a715-5ebe59c84c88")
                .claim("nome", "Administrador")
                .claim("cargo", cargo)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 28800000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
