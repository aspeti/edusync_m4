package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface GestionEscolarJpaRepository
    extends JpaRepository<GestionEscolarJpaEntity, UUID>, JpaSpecificationExecutor<GestionEscolarJpaEntity> {

  /** {@code DD-UC-021}: resuelve la unica gestion {@code ACTIVA} de un tenant. */
  Optional<GestionEscolarJpaEntity> findFirstByTenantIdAndEstado(UUID tenantId, String estado);
}
