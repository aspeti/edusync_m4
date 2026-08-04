package com.edusync.identidad.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

  Optional<UsuarioJpaEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  List<UsuarioJpaEntity> findByTenantId(UUID tenantId);
}
