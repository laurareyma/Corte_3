package com.sabana.notificacion;

import com.sabana.notificacion.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificacionApplicationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    NotificacionRepository repository;

    @Test
    void enviarYConsultarHistorial_persisteYRecupera() throws Exception {
        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Luis\",\"nivel\":\"AMARILLO\"}"))
                .andExpect(status().isOk());

        assertThat(repository.findAll()).hasSize(1);

        mockMvc.perform(get("/api/notificaciones/historial/Luis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Luis"))
                .andExpect(jsonPath("$[0].nivel").value("AMARILLO"));
    }
}
