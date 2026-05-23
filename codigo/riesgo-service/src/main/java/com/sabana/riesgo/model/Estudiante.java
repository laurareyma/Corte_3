package com.sabana.riesgo.model;

public class Estudiante {

    private String nombre;
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
