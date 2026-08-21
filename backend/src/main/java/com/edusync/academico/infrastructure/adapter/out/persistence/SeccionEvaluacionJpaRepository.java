package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SeccionEvaluacionJpaRepository extends JpaRepository<SeccionEvaluacionJpaEntity, UUID> {

  List<SeccionEvaluacionJpaEntity> findByGestionEscolarIdAndTenantIdOrderByOrdenAsc(
      UUID gestionEscolarId, UUID tenantId);

  void deleteByGestionEscolarIdAndTenantId(UUID gestionEscolarId, UUID tenantId);
}
