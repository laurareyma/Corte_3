package com.sabana.notificacion.repository;

import com.sabana.notificacion.model.NotificacionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificacionRepositoryTest {

    @Autowired
    NotificacionRepository repository;

    @Test
    void findByNombreIgnoreCaseOrderByCreadoEnDesc_returnsOrderedMatches() {
        repository.save(new NotificacionEntity("Ana", "ROJO"));
        repository.save(new NotificacionEntity("ana", "AMARILLO"));

        List<NotificacionEntity> list = repository.findByNombreIgnoreCaseOrderByCreadoEnDesc("ANA");

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getNivel()).isEqualTo("AMARILLO");
        assertThat(list.get(1).getNivel()).isEqualTo("ROJO");
    }
}
