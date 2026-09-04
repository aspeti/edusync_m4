package com.edusync.academico.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente ({@code DD-UC-018}, {@code FSD-UC-016}): nota de un
 * {@link Estudiante} en una {@link Evaluacion}. Unicidad de negocio
 * {@code (evaluacionId, estudianteId)} del tenant. Nombre deliberado
 * {@code CalificacionEvaluacion} (diccionario FSD §6.3.2) para no colisionar con
 * {@code Calificacion} del Perfil Bolivia SIE.
 *
 * <p>Lombok solo {@code @Getter} (mismo criterio que {@link Evaluacion}).
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
      BigDecimal puntajeMaximo) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(evaluacionId, "evaluacionId no puede ser nulo");
    Objects.requireNonNull(estudianteId, "estudianteId no puede ser nulo");
    return new CalificacionEvaluacion(
        id, tenantId, evaluacionId, estudianteId, validarValor(valor, puntajeMaximo));
  }

  public static CalificacionEvaluacion reconstruir(
      CalificacionEvaluacionId id,
      UUID tenantId,
      EvaluacionId evaluacionId,
      EstudianteId estudianteId,
      BigDecimal valor) {
    return new CalificacionEvaluacion(id, tenantId, evaluacionId, estudianteId, valor);
  }

  public void actualizarValor(BigDecimal nuevoValor, BigDecimal puntajeMaximo) {
    this.valor = validarValor(nuevoValor, puntajeMaximo);
  }

  private static BigDecimal validarValor(BigDecimal valor, BigDecimal puntajeMaximo) {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
    Objects.requireNonNull(puntajeMaximo, "puntajeMaximo no puede ser nulo");
    BigDecimal normalizado = valor.setScale(2, RoundingMode.HALF_UP);
    BigDecimal max = puntajeMaximo.setScale(2, RoundingMode.HALF_UP);
    if (normalizado.compareTo(BigDecimal.ZERO) < 0 || normalizado.compareTo(max) > 0) {
      throw new RangoCalificacionInvalidoException("[0, " + max.toPlainString() + "]");
    }
    return normalizado;
  }
}
