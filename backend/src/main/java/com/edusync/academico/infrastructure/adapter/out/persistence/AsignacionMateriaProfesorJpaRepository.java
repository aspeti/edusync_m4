package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AsignacionMateriaProfesorJpaRepository
    extends JpaRepository<AsignacionMateriaProfesorJpaEntity, UUID> {

  List<AsignacionMateriaProfesorJpaEntity> findByMateriaIdAndTenantId(UUID materiaId, UUID tenantId);

  List<AsignacionMateriaProfesorJpaEntity> findByProfesorIdAndTenantId(UUID profesorId, UUID tenantId);
}
