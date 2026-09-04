package com.edusync.academico.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code calificacion_evaluacion}
 * ({@code V12__academico_calificacion_evaluacion.sql}). Nunca se expone por API.
 */
@Entity
@Table(name = "calificacion_evaluacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalificacionEvaluacionJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "evaluacion_id", nullable = false)
  private UUID evaluacionId;

  @Column(name = "estudiante_id", nullable = false)
  private UUID estudianteId;

  @Column(name = "valor", nullable = false, precision = 5, scale = 2)
  private BigDecimal valor;

  @Column(name = "actualizado_en", nullable = false)
  private Instant actualizadoEn;

  public CalificacionEvaluacionJpaEntity(
      UUID id,
      UUID tenantId,
      UUID evaluacionId,
      UUID estudianteId,
      BigDecimal valor,
      Instant actualizadoEn) {
    this.id = id;
    this.tenantId = tenantId;
    this.evaluacionId = evaluacionId;
    this.estudianteId = estudianteId;
    this.valor = valor;
    this.actualizadoEn = actualizadoEn;
  }

  void actualizarValor(BigDecimal valor, Instant actualizadoEn) {
    this.valor = valor;
    this.actualizadoEn = actualizadoEn;
  }
}
