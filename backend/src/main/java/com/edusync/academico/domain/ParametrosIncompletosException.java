package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/** Falta un parametro para alguna seccion vigente. HTTP 422 {@code E_PARAMETROS_INCOMPLETOS}. */
public class ParametrosIncompletosException extends DomainException {

  public ParametrosIncompletosException() {
    super("E_PARAMETROS_INCOMPLETOS", "Faltan parametros para las secciones vigentes del periodo");
  }
}
