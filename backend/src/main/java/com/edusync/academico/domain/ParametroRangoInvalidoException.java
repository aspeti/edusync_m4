package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/** Rango de {@link ParametroPeriodo} invalido. HTTP 422 {@code E_RANGO_INVALIDO}. */
public class ParametroRangoInvalidoException extends DomainException {

  public ParametroRangoInvalidoException() {
    super("E_RANGO_INVALIDO", "El rango del parametro no es valido para la seccion");
  }
}
