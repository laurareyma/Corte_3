package com.sabana.riesgo.factory;

import com.sabana.riesgo.model.NivelRiesgo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApoyoFactoryTest {

    @Test
    void crearApoyo_nivelRojo_retornaApoyoPsicologico() {
        Apoyo apoyo = ApoyoFactory.crearApoyo(NivelRiesgo.ROJO);
        assertThat(apoyo).isInstanceOf(ApoyoPsicologico.class);
        assertThat(apoyo.ofrecer()).isNotBlank();
    }

    @Test
    void crearApoyo_nivelAmarillo_retornaApoyoAcademico() {
        Apoyo apoyo = ApoyoFactory.crearApoyo(NivelRiesgo.AMARILLO);
        assertThat(apoyo).isInstanceOf(ApoyoAcademico.class);
        assertThat(apoyo.ofrecer()).isNotBlank();
    }

    @Test
    void crearApoyo_nivelVerde_lanzaExcepcion() {
        assertThatThrownBy(() -> ApoyoFactory.crearApoyo(NivelRiesgo.VERDE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VERDE");
    }
}
