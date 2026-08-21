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
 * Entidad JPA de {@code paralelo} (tabla con {@code tenant_id} obligatorio y politica RLS,
 * {@code V6__academico_curso_paralelo.sql}). {@code tenant_id} es una columna redundante
 * respecto al diccionario de datos del FSD (derivable via join contra {@code curso}),
 * añadida deliberadamente para RLS directa/aislamiento defensivo (ver {@code DD-UC-010}
 * &sect;2). Nunca se expone directamente por API (AGENTS.md &sect;5).
 */
@Entity
@Table(name = "paralelo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParaleloJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "curso_id", nullable = false)
  private UUID cursoId;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  public ParaleloJpaEntity(UUID id, UUID tenantId, UUID cursoId, String nombre) {
    this.id = id;
    this.tenantId = tenantId;
    this.cursoId = cursoId;
    this.nombre = nombre;
  }
}
