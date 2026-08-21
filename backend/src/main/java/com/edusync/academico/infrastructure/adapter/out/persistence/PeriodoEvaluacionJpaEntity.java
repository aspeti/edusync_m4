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
 * Entidad JPA de {@code periodo_evaluacion} ({@code V9__academico_periodo_evaluacion.sql}).
 * Nunca se expone por API.
 */
@Entity
@Table(name = "periodo_evaluacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PeriodoEvaluacionJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "gestion_escolar_id", nullable = false)
  private UUID gestionEscolarId;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  @Column(name = "fecha_inicio", nullable = false)
  private LocalDate fechaInicio;

  @Column(name = "fecha_fin", nullable = false)
  private LocalDate fechaFin;

  @Column(name = "orden", nullable = false)
  private int orden;

  @Column(name = "estado", nullable = false)
  private String estado;

  public PeriodoEvaluacionJpaEntity(
      UUID id,
      UUID tenantId,
      UUID gestionEscolarId,
      String nombre,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      int orden,
      String estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.gestionEscolarId = gestionEscolarId;
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.orden = orden;
    this.estado = estado;
  }
}
