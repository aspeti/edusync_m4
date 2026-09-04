package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/** HTTP 422 {@code E_EVALUACION_YA_ANULADA}: no se anula ni edita una eval ya {@code ANULADA}. */
public class EvaluacionYaAnuladaException extends DomainException {

  public EvaluacionYaAnuladaException() {
    super("E_EVALUACION_YA_ANULADA", "La evaluacion ya esta anulada");
  }
}
