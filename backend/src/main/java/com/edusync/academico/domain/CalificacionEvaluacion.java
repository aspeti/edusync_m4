package com.edusync.academico.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente ({@code DD-UC-018}/{@code DD-UC-034}, {@code FSD-UC-016}):
 * nota de un {@link Estudiante} en una {@link Evaluacion}. El valor se valida en
 * {@code [seccion.rangoMin, seccion.rangoMax]} ({@code ADR-0027}).
 */
@Getter
public final class CalificacionEvaluacion {

  private final CalificacionEvaluacionId id;
  private final UUID tenantId;
  private final EvaluacionId evaluacionId;
  private final EstudianteId estudianteId;
  private BigDecimal valor;

  private CalificacionEvaluacion(
      CalificacionEvaluacionId id,
      UUID tenantId,
      EvaluacionId evaluacionId,
      EstudianteId estudianteId,
      BigDecimal valor) {
    this.id = id;
    this.tenantId = tenantId;
    this.evaluacionId = evaluacionId;
    this.estudianteId = estudianteId;
    this.valor = valor;
  }

  public static CalificacionEvaluacion crear(
      CalificacionEvaluacionId id,
      UUID tenantId,
      EvaluacionId evaluacionId,
      EstudianteId estudianteId,
      BigDecimal valor,
      BigDecimal rangoMin,
      BigDecimal rangoMax) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(evaluacionId, "evaluacionId no puede ser nulo");
    Objects.requireNonNull(estudianteId, "estudianteId no puede ser nulo");
    return new CalificacionEvaluacion(
        id, tenantId, evaluacionId, estudianteId, validarValor(valor, rangoMin, rangoMax));
  }

  public static CalificacionEvaluacion reconstruir(
      CalificacionEvaluacionId id,
      UUID tenantId,
      EvaluacionId evaluacionId,
      EstudianteId estudianteId,
      BigDecimal valor) {
    return new CalificacionEvaluacion(id, tenantId, evaluacionId, estudianteId, valor);
  }

  public void actualizarValor(BigDecimal nuevoValor, BigDecimal rangoMin, BigDecimal rangoMax) {
    this.valor = validarValor(nuevoValor, rangoMin, rangoMax);
  }

  private static BigDecimal validarValor(BigDecimal valor, BigDecimal rangoMin, BigDecimal rangoMax) {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
    Objects.requireNonNull(rangoMin, "rangoMin no puede ser nulo");
    Objects.requireNonNull(rangoMax, "rangoMax no puede ser nulo");
    BigDecimal normalizado = valor.setScale(2, RoundingMode.HALF_UP);
    BigDecimal min = rangoMin.setScale(2, RoundingMode.HALF_UP);
    BigDecimal max = rangoMax.setScale(2, RoundingMode.HALF_UP);
    if (normalizado.compareTo(min) < 0 || normalizado.compareTo(max) > 0) {
      throw new RangoCalificacionInvalidoException(
          "[" + min.toPlainString() + ", " + max.toPlainString() + "]");
    }
    return normalizado;
  }
}
