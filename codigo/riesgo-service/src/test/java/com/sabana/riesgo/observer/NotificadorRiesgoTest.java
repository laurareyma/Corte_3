package com.sabana.riesgo.observer;

import com.sabana.riesgo.model.Estudiante;
import com.sabana.riesgo.model.NivelRiesgo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificadorRiesgoTest {

    @Test
    void actualizar_estudianteEnRiesgo_noLanzaExcepcion() {
        NotificadorRiesgo notificador = new NotificadorRiesgo();
        Estudiante e = new Estudiante("Laura", NivelRiesgo.ROJO);

        assertThatCode(() -> notificador.actualizar(e)).doesNotThrowAnyException();
    }
}
