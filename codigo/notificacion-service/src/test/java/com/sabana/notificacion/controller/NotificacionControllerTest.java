package com.sabana.notificacion.controller;

import com.sabana.notificacion.model.NotificacionRequest;
import com.sabana.notificacion.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificacionService notificacionService;

    @Test
    void enviar_returnsOk() throws Exception {
        when(notificacionService.procesarNotificacion(any(NotificacionRequest.class)))
                .thenReturn("✅ Notificación registrada para Maria con nivel de riesgo ROJO");

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Maria\",\"nivel\":\"ROJO\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Notificación registrada para Maria con nivel de riesgo ROJO"));
    }

    @Test
    void historial_returnsJsonArray() throws Exception {
        when(notificacionService.consultarHistorial("Maria"))
                .thenReturn(List.of(Map.of("nombre", "Maria", "nivel", "ROJO")));

        mockMvc.perform(get("/api/notificaciones/historial/Maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Maria"))
                .andExpect(jsonPath("$[0].nivel").value("ROJO"));
    }
}
