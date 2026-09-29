package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/** Regla distinta de {@code PROMEDIO_SIMPLE}. HTTP 422 {@code E_REGLA_NO_SOPORTADA}. */
public class ReglaCombinacionNoSoportadaException extends DomainException {

  public ReglaCombinacionNoSoportadaException() {
    super("E_REGLA_NO_SOPORTADA", "La regla de combinacion no esta soportada");
  }
}
