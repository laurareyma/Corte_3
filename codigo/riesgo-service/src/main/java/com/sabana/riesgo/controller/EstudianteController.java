package com.sabana.riesgo.controller;

import com.sabana.riesgo.model.Estudiante;
import com.sabana.riesgo.service.EvaluadorRiesgoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private static final Logger log = LoggerFactory.getLogger(EstudianteController.class);
    private final EvaluadorRiesgoService evaluadorService;

    public EstudianteController(EvaluadorRiesgoService evaluadorService) {
        this.evaluadorService = evaluadorService;
    }

    // ✅ Endpoint 1: Evaluar riesgo (protegido con JWT)
    @PostMapping("/evaluar")
    public ResponseEntity<Map<String, String>> evaluarRiesgo(@Valid @RequestBody Estudiante estudiante) {
        log.info("📥 POST /api/estudiantes/evaluar - Estudiante: {}", estudiante.getNombre());
        Map<String, String> resultado = evaluadorService.evaluar(estudiante);
        return ResponseEntity.ok(resultado);
    }

    // ✅ Endpoint 2: Consultar historial (protegido con JWT)
    @GetMapping("/historial/{nombre}")
    public ResponseEntity<Map<String, String>> consultarHistorial(@PathVariable String nombre) {
        log.info("📥 GET /api/estudiantes/historial/{}", nombre);
        Map<String, String> historial = evaluadorService.consultarHistorial(nombre);
        return ResponseEntity.ok(historial);
    }

    // ✅ Endpoint público: health check del servicio
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "riesgo-service"));
    }
}
