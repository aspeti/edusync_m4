package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/** DELETE del ultimo {@link PeriodoEvaluacion} de una gestion (N debe permanecer &ge; 1). */
public class PeriodoUnicoException extends DomainException {

  public PeriodoUnicoException() {
    super("E_PERIODO_UNICO", "La gestion debe conservar al menos un periodo de evaluacion");
  }
}
