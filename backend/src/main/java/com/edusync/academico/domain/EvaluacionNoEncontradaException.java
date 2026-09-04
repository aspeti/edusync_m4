package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/** HTTP 404 {@code E_EVALUACION_NO_ENCONTRADA} (inexistente o de otro tenant). */
public class EvaluacionNoEncontradaException extends DomainException {

  public EvaluacionNoEncontradaException() {
    super("E_EVALUACION_NO_ENCONTRADA", "Evaluacion no encontrada");
  }
}
