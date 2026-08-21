package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface InscripcionJpaRepository extends JpaRepository<InscripcionJpaEntity, UUID> {

  List<InscripcionJpaEntity> findByEstudianteIdAndTenantId(UUID estudianteId, UUID tenantId);

  boolean existsByEstudianteIdAndGestionEscolarIdAndTenantId(
      UUID estudianteId, UUID gestionEscolarId, UUID tenantId);
}
