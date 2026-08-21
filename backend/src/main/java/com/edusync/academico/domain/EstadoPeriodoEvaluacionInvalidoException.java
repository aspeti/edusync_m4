package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Transicion de {@link EstadoPeriodoEvaluacion} fuera de {@code PENDIENTE -> ABIERTO} y
 * {@code ABIERTO -> CERRADO}.
 */
public class EstadoPeriodoEvaluacionInvalidoException extends DomainException {

  public EstadoPeriodoEvaluacionInvalidoException(
      EstadoPeriodoEvaluacion actual, EstadoPeriodoEvaluacion solicitado) {
    super("E_ESTADO_INVALIDO", "No se puede transicionar de " + actual + " a " + solicitado);
  }
}
