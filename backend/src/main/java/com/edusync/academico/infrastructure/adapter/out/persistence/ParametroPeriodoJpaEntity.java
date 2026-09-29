package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.domain.ReglaCombinacionPeriodo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parametro_periodo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParametroPeriodoJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "periodo_evaluacion_id", nullable = false)
  private UUID periodoEvaluacionId;

  @Column(name = "seccion_evaluacion_id", nullable = false)
  private UUID seccionEvaluacionId;

  @Column(name = "rango_min", nullable = false, precision = 5, scale = 2)
  private BigDecimal rangoMin;

  @Column(name = "rango_max", nullable = false, precision = 5, scale = 2)
  private BigDecimal rangoMax;

  @Enumerated(EnumType.STRING)
  @Column(name = "regla_combinacion", nullable = false, length = 30)
  private ReglaCombinacionPeriodo reglaCombinacion;

  public ParametroPeriodoJpaEntity(
      UUID id,
      UUID tenantId,
      UUID periodoEvaluacionId,
      UUID seccionEvaluacionId,
      BigDecimal rangoMin,
      BigDecimal rangoMax,
      ReglaCombinacionPeriodo reglaCombinacion) {
    this.id = id;
    this.tenantId = tenantId;
    this.periodoEvaluacionId = periodoEvaluacionId;
    this.seccionEvaluacionId = seccionEvaluacionId;
    this.rangoMin = rangoMin;
    this.rangoMax = rangoMax;
    this.reglaCombinacion = reglaCombinacion;
  }
}
