package com.sabana.notificacion.repository;

import com.sabana.notificacion.model.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long> {

    List<NotificacionEntity> findByNombreIgnoreCaseOrderByCreadoEnDesc(String nombre);
}
