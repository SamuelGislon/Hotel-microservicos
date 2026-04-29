package com.user.api_users;

import com.user.api_users.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "hosped@Secret#2025!xK9mPqL3nZvB8wR");
        ReflectionTestUtils.setField(jwtUtil, "issuer", "hosped-users");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 28800000L);
    }

    @Test
    void gerarTokenComClaimsEsperadas() {
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.gerar(userId, "12345678900", "Administrador", "ADMINISTRADOR");

        assertTrue(jwtUtil.isValid(token));
        assertEquals(userId.toString(), jwtUtil.getUserId(token));
        assertEquals("12345678900", jwtUtil.getCpf(token));
        assertEquals("Administrador", jwtUtil.getNome(token));
        assertEquals("ADMINISTRADOR", jwtUtil.getCargo(token));
    }

    @Test
    void tokenMalformadoEhInvalido() {
        assertFalse(jwtUtil.isValid("token-invalido"));
    }
}
