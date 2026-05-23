package com.sabana.notificacion.controller;

import com.sabana.notificacion.model.NotificacionRequest;
import com.sabana.notificacion.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private static final Logger log = LoggerFactory.getLogger(NotificacionController.class);
    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    // ✅ Endpoint 1: Recibir notificación desde riesgo-service (comunicación REST)
    @PostMapping("/enviar")
    public ResponseEntity<String> recibirNotificacion(@RequestBody NotificacionRequest request) {
        log.info("📥 POST /api/notificaciones/enviar — Estudiante: {}", request.getNombre());
        String resultado = notificacionService.procesarNotificacion(request);
        return ResponseEntity.ok(resultado);
    }

    // ✅ Endpoint 2: Consultar historial de un estudiante
    @GetMapping("/historial/{nombre}")
    public ResponseEntity<List<Map<String, String>>> consultarHistorial(@PathVariable String nombre) {
        log.info("📥 GET /api/notificaciones/historial/{}", nombre);
        return ResponseEntity.ok(notificacionService.consultarHistorial(nombre));
    }

    // Health check público
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "notificacion-service"));
    }
}
