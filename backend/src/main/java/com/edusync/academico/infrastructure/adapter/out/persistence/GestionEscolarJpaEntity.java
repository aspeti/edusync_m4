package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code gestion_escolar} (tabla con {@code tenant_id} obligatorio y
 * politica RLS, {@code V5__academico_gestion_escolar.sql}). Nunca se expone directamente
 * por API (AGENTS.md &sect;5): {@code GestionEscolarRepositoryAdapter} la traduce a/desde
 * {@code academico.domain.GestionEscolar}. Lombok sin restriccion en {@code infrastructure/}
 * ({@code ADR-0012}).
 */
@Entity
@Table(name = "gestion_escolar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GestionEscolarJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  @Column(name = "fecha_inicio", nullable = false)
  private LocalDate fechaInicio;

  @Column(name = "fecha_fin", nullable = false)
  private LocalDate fechaFin;

  @Column(name = "estado", nullable = false)
  private String estado;

  public GestionEscolarJpaEntity(
      UUID id, UUID tenantId, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.estado = estado;
  }
}
