package com.edusync.academico.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface MateriaJpaRepository
    extends JpaRepository<MateriaJpaEntity, UUID>, JpaSpecificationExecutor<MateriaJpaEntity> {}
