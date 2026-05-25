package com.sabana.notificacion.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class NotificacionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String nombre;

    @NotBlank(message = "El nivel de riesgo es obligatorio")
    @Pattern(regexp = "VERDE|AMARILLO|ROJO", message = "El nivel debe ser VERDE, AMARILLO o ROJO")
    private String nivel;

    public NotificacionRequest() {}

    public NotificacionRequest(String nombre, String nivel) {
        this.nombre = nombre;
        this.nivel = nivel;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
}
