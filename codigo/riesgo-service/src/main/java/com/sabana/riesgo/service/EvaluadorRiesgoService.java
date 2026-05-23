package com.sabana.riesgo.service;

import com.sabana.riesgo.client.NotificacionClient;
import com.sabana.riesgo.factory.Apoyo;
import com.sabana.riesgo.factory.ApoyoFactory;
import com.sabana.riesgo.model.Estudiante;
import com.sabana.riesgo.model.NivelRiesgo;
import com.sabana.riesgo.observer.RiesgoObserver;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EvaluadorRiesgoService {

    private static final Logger log = LoggerFactory.getLogger(EvaluadorRiesgoService.class);

    private final List<RiesgoObserver> observadores;
    private final NotificacionClient notificacionClient;

    // ✅ Métricas personalizadas para Prometheus
    private final Counter estudiantesEvaluadosCounter;
    private final Counter alertasCriticasCounter;

    public EvaluadorRiesgoService(NotificacionClient notificacionClient,
                                   MeterRegistry meterRegistry,
                                   List<RiesgoObserver> observadores) {
        this.notificacionClient = notificacionClient;
        this.observadores = observadores;

        this.estudiantesEvaluadosCounter = Counter.builder("estudiantes.evaluados.total")
                .description("Total de estudiantes evaluados")
                .register(meterRegistry);

        this.alertasCriticasCounter = Counter.builder("alertas.criticas.total")
                .description("Total de alertas de riesgo crítico (ROJO)")
                .register(meterRegistry);
    }

    // ✅ Funcionalidad 1: evaluar riesgo de un estudiante
    public Map<String, String> evaluar(Estudiante estudiante) {
        log.info("🔍 Evaluando riesgo de: {}", estudiante.getNombre());
        estudiantesEvaluadosCounter.increment();

        if (estudiante.getNivel() == NivelRiesgo.ROJO ||
            estudiante.getNivel() == NivelRiesgo.AMARILLO) {

            // Patrón Observer: notificar localmente
            for (RiesgoObserver obs : observadores) {
                obs.actualizar(estudiante);
            }

            // Patrón Factory: asignar apoyo según nivel
            Apoyo apoyo = ApoyoFactory.crearApoyo(estudiante.getNivel());
            String apoyoMsg = apoyo.ofrecer();

            if (estudiante.getNivel() == NivelRiesgo.ROJO) {
                alertasCriticasCounter.increment();
            }

            // Comunicación REST al notificacion-service (Circuit Breaker + Retry + TimeLimiter + timeouts HTTP)
            String respuestaNotificacion = notificacionClient.enviarNotificacion(estudiante).join();

            log.info("📋 Evaluación completada para: {}", estudiante.getNombre());

            return Map.of(
                "estudiante",    estudiante.getNombre(),
                "nivelRiesgo",   estudiante.getNivel().name(),
                "apoyo",         apoyoMsg,
                "notificacion",  respuestaNotificacion
            );
        } else {
            log.info("✅ Estudiante {} sin riesgo crítico.", estudiante.getNombre());
            return Map.of(
                "estudiante",  estudiante.getNombre(),
                "nivelRiesgo", "VERDE",
                "mensaje",     "✅ Sin riesgo crítico. ¡Continúa así!"
            );
        }
    }

    // ✅ Funcionalidad 2: consultar historial de notificaciones
    public Map<String, String> consultarHistorial(String nombreEstudiante) {
        log.info("📂 Consultando historial de: {}", nombreEstudiante);
        String historial = notificacionClient.consultarHistorial(nombreEstudiante).join();
        return Map.of("estudiante", nombreEstudiante, "historial", historial);
    }
}
