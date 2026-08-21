package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code curso} (tabla con {@code tenant_id} obligatorio y politica RLS,
 * {@code V6__academico_curso_paralelo.sql}). Nunca se expone directamente por API
 * (AGENTS.md &sect;5): {@code CursoRepositoryAdapter} la traduce a/desde
 * {@code academico.domain.Curso}. Lombok sin restriccion en {@code infrastructure/}
 * ({@code ADR-0012}).
 */
@Entity
@Table(name = "curso")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CursoJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  public CursoJpaEntity(UUID id, UUID tenantId, String nombre) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombre = nombre;
  }
}
