package com.edusync.identidad.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface UsuarioJpaRepository
    extends JpaRepository<UsuarioJpaEntity, UUID>, JpaSpecificationExecutor<UsuarioJpaEntity> {

  Optional<UsuarioJpaEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  List<UsuarioJpaEntity> findByTenantId(UUID tenantId);
}
