package com.edusync.academico.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Parametros normativos de una {@link SeccionEvaluacion} dentro de un
 * {@link PeriodoEvaluacion} ({@code DD-UC-029}, {@code FSD-UC-009}, {@code ADR-0002}).
 * El peso sigue siendo {@code SeccionEvaluacion.nota}; aqui solo viven el rango y la regla.
 *
 * <p>Lombok solo {@code @Getter}.
 */
@Getter
public final class ParametroPeriodo {

  private final ParametroPeriodoId id;
  private final UUID tenantId;
  private final PeriodoEvaluacionId periodoEvaluacionId;
  private final SeccionEvaluacionId seccionEvaluacionId;
  private final BigDecimal rangoMin;
  private final BigDecimal rangoMax;
  private final ReglaCombinacionPeriodo reglaCombinacion;

  private ParametroPeriodo(
      ParametroPeriodoId id,
      UUID tenantId,
      PeriodoEvaluacionId periodoEvaluacionId,
      SeccionEvaluacionId seccionEvaluacionId,
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

  public static ParametroPeriodo crear(
      ParametroPeriodoId id,
      UUID tenantId,
      PeriodoEvaluacionId periodoEvaluacionId,
      SeccionEvaluacionId seccionEvaluacionId,
      BigDecimal rangoMin,
      BigDecimal rangoMax,
      ReglaCombinacionPeriodo reglaCombinacion) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(periodoEvaluacionId, "periodoEvaluacionId no puede ser nulo");
    Objects.requireNonNull(seccionEvaluacionId, "seccionEvaluacionId no puede ser nulo");
    Objects.requireNonNull(reglaCombinacion, "reglaCombinacion no puede ser nula");
    BigDecimal min = normalizar(rangoMin);
    BigDecimal max = normalizar(rangoMax);
    if (min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(min) <= 0) {
      throw new ParametroRangoInvalidoException();
    }
    return new ParametroPeriodo(
        id, tenantId, periodoEvaluacionId, seccionEvaluacionId, min, max, reglaCombinacion);
  }

  public static ParametroPeriodo reconstruir(
      ParametroPeriodoId id,
      UUID tenantId,
      PeriodoEvaluacionId periodoEvaluacionId,
      SeccionEvaluacionId seccionEvaluacionId,
      BigDecimal rangoMin,
      BigDecimal rangoMax,
      ReglaCombinacionPeriodo reglaCombinacion) {
    return new ParametroPeriodo(
        id, tenantId, periodoEvaluacionId, seccionEvaluacionId, rangoMin, rangoMax, reglaCombinacion);
  }

  private static BigDecimal normalizar(BigDecimal valor) {
    Objects.requireNonNull(valor, "rango no puede ser nulo");
    return valor.setScale(2, RoundingMode.HALF_UP);
  }
}
