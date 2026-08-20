package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface GestionEscolarJpaRepository
    extends JpaRepository<GestionEscolarJpaEntity, UUID>, JpaSpecificationExecutor<GestionEscolarJpaEntity> {
}
