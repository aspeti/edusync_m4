package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ParametroPeriodoJpaRepository extends JpaRepository<ParametroPeriodoJpaEntity, UUID> {

  List<ParametroPeriodoJpaEntity> findByPeriodoEvaluacionIdAndTenantId(UUID periodoEvaluacionId, UUID tenantId);

  void deleteByPeriodoEvaluacionIdAndTenantId(UUID periodoEvaluacionId, UUID tenantId);
}
