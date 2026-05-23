package com.sabana.riesgo.controller;

import com.sabana.riesgo.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ Endpoint público para obtener token JWT (para demo/presentación)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String usuario = body.get("usuario");
        if (usuario != null && !usuario.isBlank()) {
            String token = jwtUtil.generarToken(usuario);
            return ResponseEntity.ok(Map.of(
                "token",   token,
                "usuario", usuario,
                "tipo",    "Bearer"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Usuario requerido"));
    }
}
