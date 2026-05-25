package com.sabana.riesgo.factory;

import com.sabana.riesgo.model.NivelRiesgo;

public class ApoyoFactory {

    public static Apoyo crearApoyo(NivelRiesgo nivel) {
        return switch (nivel) {
            case ROJO     -> new ApoyoPsicologico();
            case AMARILLO -> new ApoyoAcademico();
            case VERDE    -> throw new IllegalArgumentException(
                    "No se requiere apoyo para nivel VERDE");
        };
    }
}
