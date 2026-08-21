package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PeriodoEvaluacionJpaRepository extends JpaRepository<PeriodoEvaluacionJpaEntity, UUID> {

  List<PeriodoEvaluacionJpaEntity> findByGestionEscolarIdAndTenantIdOrderByOrdenAsc(
      UUID gestionEscolarId, UUID tenantId);
}
