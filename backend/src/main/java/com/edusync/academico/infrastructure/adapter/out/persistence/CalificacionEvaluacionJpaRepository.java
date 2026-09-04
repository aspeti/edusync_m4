package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CalificacionEvaluacionJpaRepository
    extends JpaRepository<CalificacionEvaluacionJpaEntity, UUID> {

  Optional<CalificacionEvaluacionJpaEntity> findByEvaluacionIdAndEstudianteIdAndTenantId(
      UUID evaluacionId, UUID estudianteId, UUID tenantId);

  List<CalificacionEvaluacionJpaEntity> findByEvaluacionIdAndTenantId(
      UUID evaluacionId, UUID tenantId);

  List<CalificacionEvaluacionJpaEntity> findByEvaluacionIdInAndEstudianteIdAndTenantId(
      Collection<UUID> evaluacionIds, UUID estudianteId, UUID tenantId);
}
