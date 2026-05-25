package com.sabana.notificacion.service;

import com.sabana.notificacion.model.NotificacionEntity;
import com.sabana.notificacion.model.NotificacionRequest;
import com.sabana.notificacion.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    NotificacionRepository repository;

    @InjectMocks
    NotificacionService service;

    // -----------------------------------------------------------------------
    // procesarNotificacion
    // -----------------------------------------------------------------------

    @Test
    void procesarNotificacion_guardaEntidadConDatosCorrectos() {
        NotificacionRequest request = new NotificacionRequest("Maria", "ROJO");
        when(repository.save(any(NotificacionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.procesarNotificacion(request);

        ArgumentCaptor<NotificacionEntity> captor = ArgumentCaptor.forClass(NotificacionEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Maria");
        assertThat(captor.getValue().getNivel()).isEqualTo("ROJO");
    }

    @Test
    void procesarNotificacion_retornaMensajeConfirmacion() {
        NotificacionRequest request = new NotificacionRequest("Carlos", "AMARILLO");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String resultado = service.procesarNotificacion(request);

        assertThat(resultado).contains("Carlos").contains("AMARILLO");
    }

    // -----------------------------------------------------------------------
    // consultarHistorial
    // -----------------------------------------------------------------------

    @Test
    void consultarHistorial_retornaMapConNivelYTimestamp() {
        NotificacionEntity entity = new NotificacionEntity("Ana", "ROJO");
        when(repository.findByNombreIgnoreCaseOrderByCreadoEnDesc("Ana"))
                .thenReturn(List.of(entity));

        List<Map<String, String>> resultado = service.consultarHistorial("Ana");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).containsKeys("nombre", "nivel", "creadoEn");
        assertThat(resultado.get(0).get("nombre")).isEqualTo("Ana");
        assertThat(resultado.get(0).get("nivel")).isEqualTo("ROJO");
    }

    @Test
    void consultarHistorial_sinRegistros_retornaListaVacia() {
        when(repository.findByNombreIgnoreCaseOrderByCreadoEnDesc("Nadie"))
                .thenReturn(List.of());

        List<Map<String, String>> resultado = service.consultarHistorial("Nadie");

        assertThat(resultado).isEmpty();
        verify(repository).findByNombreIgnoreCaseOrderByCreadoEnDesc("Nadie");
    }

    @Test
    void consultarHistorial_consultaBuscaInsensibleAMayusculas() {
        when(repository.findByNombreIgnoreCaseOrderByCreadoEnDesc("CARLOS"))
                .thenReturn(List.of());

        service.consultarHistorial("CARLOS");

        verify(repository).findByNombreIgnoreCaseOrderByCreadoEnDesc("CARLOS");
    }
}
