package com.sabana.riesgo.controller;

import com.sabana.riesgo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private JwtUtil jwtUtil;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        controller = new AuthController(jwtUtil);
    }

    @Test
    void login_usuarioValido_retornaTokenYDatos() {
        when(jwtUtil.generarToken("admin")).thenReturn("token-jwt-123");

        ResponseEntity<Map<String, String>> resp = controller.login(Map.of("usuario", "admin"));

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).containsEntry("token", "token-jwt-123");
        assertThat(resp.getBody()).containsEntry("usuario", "admin");
        assertThat(resp.getBody()).containsEntry("tipo", "Bearer");
    }

    @Test
    void login_usuarioEnBlanco_retornaBadRequest() {
        ResponseEntity<Map<String, String>> resp = controller.login(Map.of("usuario", "  "));

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void login_sinCampoUsuario_retornaBadRequest() {
        Map<String, String> body = new HashMap<>();
        body.put("otro", "dato");

        ResponseEntity<Map<String, String>> resp = controller.login(body);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }
}
