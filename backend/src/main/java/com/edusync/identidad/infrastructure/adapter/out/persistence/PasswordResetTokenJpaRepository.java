package com.edusync.identidad.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {

  Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);
}
