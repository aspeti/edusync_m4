package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AsignacionMateriaCursoJpaRepository extends JpaRepository<AsignacionMateriaCursoJpaEntity, UUID> {

  List<AsignacionMateriaCursoJpaEntity> findByMateriaIdAndTenantId(UUID materiaId, UUID tenantId);

  boolean existsByMateriaIdAndCursoIdAndParaleloIdAndTenantId(
      UUID materiaId, UUID cursoId, UUID paraleloId, UUID tenantId);
}
