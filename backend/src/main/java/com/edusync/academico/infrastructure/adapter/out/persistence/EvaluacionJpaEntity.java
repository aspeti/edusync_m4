package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code evaluacion} ({@code V11__academico_evaluacion.sql}).
 * Nunca se expone por API.
 */
@Entity
@Table(name = "evaluacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluacionJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "materia_id", nullable = false)
  private UUID materiaId;

  @Column(name = "periodo_evaluacion_id", nullable = false)
  private UUID periodoEvaluacionId;

  @Column(name = "seccion_evaluacion_id", nullable = false)
  private UUID seccionEvaluacionId;

  @Column(name = "nombre", nullable = false, length = 100)
  private String nombre;

  @Column(name = "fecha", nullable = false)
  private LocalDate fecha;

  @Column(name = "puntaje_maximo", nullable = false, precision = 5, scale = 2)
  private BigDecimal puntajeMaximo;

  @Column(name = "descripcion")
  private String descripcion;

  @Column(name = "estado", nullable = false, length = 20)
  private String estado;

  public EvaluacionJpaEntity(
      UUID id,
      UUID tenantId,
      UUID materiaId,
      UUID periodoEvaluacionId,
      UUID seccionEvaluacionId,
      String nombre,
      LocalDate fecha,
      BigDecimal puntajeMaximo,
      String descripcion,
      String estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.materiaId = materiaId;
    this.periodoEvaluacionId = periodoEvaluacionId;
    this.seccionEvaluacionId = seccionEvaluacionId;
    this.nombre = nombre;
    this.fecha = fecha;
    this.puntajeMaximo = puntajeMaximo;
    this.descripcion = descripcion;
    this.estado = estado;
  }
}
