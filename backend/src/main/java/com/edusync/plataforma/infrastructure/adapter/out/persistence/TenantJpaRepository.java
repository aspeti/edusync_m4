package com.edusync.plataforma.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, UUID> {

  List<TenantJpaEntity> findByEstadoNotAndFechaVencimientoSuscripcionBefore(String estadoExcluido, LocalDate fecha);
}
