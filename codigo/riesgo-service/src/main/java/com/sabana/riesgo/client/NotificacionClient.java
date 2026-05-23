package com.sabana.riesgo.client;

import com.sabana.riesgo.model.Estudiante;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class NotificacionClient {

    private static final Logger log = LoggerFactory.getLogger(NotificacionClient.class);

    private final RestTemplate restTemplate;

    @Value("${notificacion.service.url}")
    private String notificacionServiceUrl;

    public NotificacionClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Circuit Breaker + Retry + TimeLimiter: la llamada remota se ejecuta de forma asíncrona
     * para que Resilience4j pueda aplicar {@code timeout-duration} (ver application.properties).
     * Los timeouts HTTP del {@link RestTemplate} acotan además el socket connect/read.
     */
    @CircuitBreaker(name = "notificacionService", fallbackMethod = "fallbackNotificar")
    @Retry(name = "notificacionService")
    @TimeLimiter(name = "notificacionService")
    public CompletableFuture<String> enviarNotificacion(Estudiante estudiante) {
        return CompletableFuture.supplyAsync(() -> {
            String url = notificacionServiceUrl + "/api/notificaciones/enviar";

            Map<String, String> body = Map.of(
                    "nombre", estudiante.getNombre(),
                    "nivel", estudiante.getNivel().name()
            );

            log.info("Enviando notificación al notificacion-service para: {}", estudiante.getNombre());
            String response = restTemplate.postForObject(url, body, String.class);
            log.info("Respuesta del notificacion-service: {}", response);
            return response;
        });
    }

    public CompletableFuture<String> fallbackNotificar(Estudiante estudiante, Throwable ex) {
        log.error("Circuit Breaker / timeout / fallo al notificar {}. Causa: {}",
                estudiante.getNombre(), ex.getMessage());
        return CompletableFuture.completedFuture(
                "⚠️ Notificación no disponible temporalmente. Riesgo registrado localmente.");
    }

    @CircuitBreaker(name = "notificacionService", fallbackMethod = "fallbackHistorial")
    @Retry(name = "notificacionService")
    @TimeLimiter(name = "notificacionService")
    public CompletableFuture<String> consultarHistorial(String nombreEstudiante) {
        return CompletableFuture.supplyAsync(() -> {
            String url = notificacionServiceUrl + "/api/notificaciones/historial/" + nombreEstudiante;
            log.info("Consultando historial de notificaciones para: {}", nombreEstudiante);
            return restTemplate.getForObject(url, String.class);
        });
    }

    public CompletableFuture<String> fallbackHistorial(String nombreEstudiante, Throwable ex) {
        log.error("Historial no disponible para {}. Causa: {}", nombreEstudiante, ex.getMessage());
        return CompletableFuture.completedFuture("[]");
    }
}
