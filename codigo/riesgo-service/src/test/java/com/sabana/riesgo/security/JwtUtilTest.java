package com.sabana.riesgo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "clave-super-secreta-sabana-2024-debe-ser-larga-minimo-32-chars";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    }

    @Test
    void generarToken_retornaTokenNoVacio() {
        String token = jwtUtil.generarToken("admin");
        assertThat(token).isNotBlank();
    }

    @Test
    void extraerUsuario_retornaElMismoUsuario() {
        String token = jwtUtil.generarToken("laura");
        assertThat(jwtUtil.extraerUsuario(token)).isEqualTo("laura");
    }

    @Test
    void validarToken_tokenValido_retornaTrue() {
        String token = jwtUtil.generarToken("juan");
        assertThat(jwtUtil.validarToken(token)).isTrue();
    }

    @Test
    void validarToken_tokenManipulado_retornaFalse() {
        String token = jwtUtil.generarToken("ana");
        String tokenRoto = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtUtil.validarToken(tokenRoto)).isFalse();
    }

    @Test
    void validarToken_cadenaVacia_retornaFalse() {
        assertThat(jwtUtil.validarToken("")).isFalse();
    }

    @Test
    void generarToken_dosCalls_producenTokensDistintos() {
        String t1 = jwtUtil.generarToken("usuario");
        String t2 = jwtUtil.generarToken("usuario");
        // Mismo usuario pero timestamps distintos → tokens distintos
        assertThat(t1).isNotEqualTo(t2);
    }
}
