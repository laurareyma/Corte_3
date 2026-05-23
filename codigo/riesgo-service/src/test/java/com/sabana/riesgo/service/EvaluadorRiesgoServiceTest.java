package com.sabana.riesgo.service;

import com.sabana.riesgo.client.NotificacionClient;
import com.sabana.riesgo.model.Estudiante;
import com.sabana.riesgo.model.NivelRiesgo;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluadorRiesgoServiceTest {

    @Mock
    NotificacionClient notificacionClient;

    private EvaluadorRiesgoService service;

    @BeforeEach
    void setUp() {
        service = new EvaluadorRiesgoService(
                notificacionClient,
                new SimpleMeterRegistry(),
                Collections.emptyList()
        );
    }

    @Test
    void evaluar_verde_noInvocaClienteNotificacion() {
        Estudiante e = new Estudiante("Ana", NivelRiesgo.VERDE);
        Map<String, String> r = service.evaluar(e);
        verifyNoInteractions(notificacionClient);
        assertThat(r.get("nivelRiesgo")).isEqualTo("VERDE");
    }

    @Test
    void evaluar_rojo_llamaNotificacionYPropagaRespuesta() {
        when(notificacionClient.enviarNotificacion(any(Estudiante.class)))
                .thenReturn(CompletableFuture.completedFuture("notif-ok"));

        Estudiante e = new Estudiante("Luis", NivelRiesgo.ROJO);
        Map<String, String> r = service.evaluar(e);

        verify(notificacionClient).enviarNotificacion(any(Estudiante.class));
        assertThat(r.get("notificacion")).isEqualTo("notif-ok");
    }

    @Test
    void consultarHistorial_delegaEnCliente() {
        when(notificacionClient.consultarHistorial("Maria"))
                .thenReturn(CompletableFuture.completedFuture("[{\"nombre\":\"Maria\"}]"));

        Map<String, String> r = service.consultarHistorial("Maria");

        assertThat(r.get("historial")).isEqualTo("[{\"nombre\":\"Maria\"}]");
    }
}
