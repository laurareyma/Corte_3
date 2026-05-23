package com.sabana.riesgo.observer;

import com.sabana.riesgo.model.Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificadorRiesgo implements RiesgoObserver {

    private static final Logger log = LoggerFactory.getLogger(NotificadorRiesgo.class);

    @Override
    public void actualizar(Estudiante estudiante) {
        log.warn("⚠️ ALERTA: {} está en nivel de riesgo {}",
                estudiante.getNombre(), estudiante.getNivel());
    }
}
