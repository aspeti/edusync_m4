package com.edusync.plataforma.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface TenantJpaRepository
    extends JpaRepository<TenantJpaEntity, UUID>, JpaSpecificationExecutor<TenantJpaEntity> {

  List<TenantJpaEntity> findByEstadoNotAndFechaVencimientoSuscripcionBefore(String estadoExcluido, LocalDate fecha);
}
