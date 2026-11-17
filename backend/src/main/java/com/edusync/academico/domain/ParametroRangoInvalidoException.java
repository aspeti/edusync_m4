package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Rangos de {@link SeccionEvaluacion} invalidos ({@code ADR-0027}).
 * HTTP 422 {@code E_RANGO_INVALIDO}.
 */
public class ParametroRangoInvalidoException extends DomainException {

  public ParametroRangoInvalidoException() {
    super("E_RANGO_INVALIDO", "El rango no es valido para la seccion (rangoMin/rangoMax/nota)");
  }
}
