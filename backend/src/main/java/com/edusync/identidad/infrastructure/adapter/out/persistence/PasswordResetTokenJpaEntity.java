package com.edusync.identidad.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code password_reset_token} (ver {@code V4__identidad_password_reset_token.sql}).
 * Solo persiste el hash SHA-256 del token, nunca el valor en claro (AGENTS.md &sect;7).
 */
@Entity
@Table(name = "password_reset_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetTokenJpaEntity {

  @Id
  private UUID id;

  @Column(name = "usuario_id", nullable = false)
  private UUID usuarioId;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "expira_en", nullable = false)
  private Instant expiraEn;

  @Column(name = "usado", nullable = false)
  private boolean usado;

  public PasswordResetTokenJpaEntity(
      UUID id, UUID usuarioId, String tokenHash, Instant expiraEn, boolean usado) {
    this.id = id;
    this.usuarioId = usuarioId;
    this.tokenHash = tokenHash;
    this.expiraEn = expiraEn;
    this.usado = usado;
  }
}
