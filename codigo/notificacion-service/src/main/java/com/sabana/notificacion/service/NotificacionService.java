package com.sabana.notificacion.service;

import com.sabana.notificacion.model.NotificacionEntity;
import com.sabana.notificacion.model.NotificacionRequest;
import com.sabana.notificacion.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository repository;

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String procesarNotificacion(NotificacionRequest request) {
        NotificacionEntity entity = new NotificacionEntity(request.getNombre(), request.getNivel());
        repository.save(entity);
        log.info("Notificación persistida — Estudiante: {} | Nivel: {}",
                request.getNombre(), request.getNivel());
        return String.format("✅ Notificación registrada para %s con nivel de riesgo %s",
                request.getNombre(), request.getNivel());
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> consultarHistorial(String nombre) {
        log.info("Consultando historial persistido para: {}", nombre);
        return repository.findByNombreIgnoreCaseOrderByCreadoEnDesc(nombre).stream()
                .map(n -> Map.of(
                        "nombre", n.getNombre(),
                        "nivel", n.getNivel()
                ))
                .collect(Collectors.toList());
    }
}
