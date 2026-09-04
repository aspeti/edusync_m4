package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Escritura de calificaciones sobre una {@link Evaluacion} que no esta
 * {@code ACTIVA}. HTTP 422 {@code E_EVALUACION_NO_ACTIVA} ({@code DD-UC-018}).
 */
public class EvaluacionNoActivaException extends DomainException {

  public EvaluacionNoActivaException() {
    super(
        "E_EVALUACION_NO_ACTIVA",
        "Solo se pueden registrar calificaciones en evaluaciones ACTIVA");
  }
}
