package com.sabana.riesgo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Estudiante {

    @NotBlank(message = "El nombre del estudiante es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String nombre;

    @NotNull(message = "El nivel de riesgo es obligatorio")
    private NivelRiesgo nivel;

    public Estudiante() {}

    public Estudiante(String nombre, NivelRiesgo nivel) {
        this.nombre = nombre;
        this.nivel = nivel;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public NivelRiesgo getNivel() { return nivel; }
    public void setNivel(NivelRiesgo nivel) { this.nivel = nivel; }
}
