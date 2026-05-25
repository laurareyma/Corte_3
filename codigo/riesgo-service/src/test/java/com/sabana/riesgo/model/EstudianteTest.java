package com.sabana.riesgo.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstudianteTest {

    @Test
    void constructorSinArgs_y_setters_funcionanCorrectamente() {
        Estudiante e = new Estudiante();
        e.setNombre("Carlos");
        e.setNivel(NivelRiesgo.AMARILLO);

        assertThat(e.getNombre()).isEqualTo("Carlos");
        assertThat(e.getNivel()).isEqualTo(NivelRiesgo.AMARILLO);
    }

    @Test
    void constructorConArgs_inicializaCamposCorrectamente() {
        Estudiante e = new Estudiante("María", NivelRiesgo.ROJO);

        assertThat(e.getNombre()).isEqualTo("María");
        assertThat(e.getNivel()).isEqualTo(NivelRiesgo.ROJO);
    }
}
