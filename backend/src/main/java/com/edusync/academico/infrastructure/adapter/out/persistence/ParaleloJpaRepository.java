package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ParaleloJpaRepository extends JpaRepository<ParaleloJpaEntity, UUID> {

  List<ParaleloJpaEntity> findByCursoIdAndTenantId(UUID cursoId, UUID tenantId);
}
