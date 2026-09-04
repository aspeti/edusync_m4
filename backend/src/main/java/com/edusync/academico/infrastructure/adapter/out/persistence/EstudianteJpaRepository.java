package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface EstudianteJpaRepository
    extends JpaRepository<EstudianteJpaEntity, UUID>, JpaSpecificationExecutor<EstudianteJpaEntity> {

  boolean existsByTenantIdAndRudeIgnoreCase(UUID tenantId, String rude);

  List<EstudianteJpaEntity> findByIdInAndTenantId(Collection<UUID> ids, UUID tenantId);
}
