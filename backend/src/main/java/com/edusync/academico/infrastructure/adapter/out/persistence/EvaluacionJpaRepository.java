package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface EvaluacionJpaRepository extends JpaRepository<EvaluacionJpaEntity, UUID> {

  List<EvaluacionJpaEntity> findByMateriaIdAndTenantId(UUID materiaId, UUID tenantId);

  List<EvaluacionJpaEntity> findByMateriaIdAndPeriodoEvaluacionIdAndTenantId(
      UUID materiaId, UUID periodoEvaluacionId, UUID tenantId);
}
