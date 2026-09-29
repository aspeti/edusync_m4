package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * El periodo no esta {@code PENDIENTE} ({@code BR-007}). HTTP 422 {@code E_PARAMETROS_INMUTABLES}.
 */
public class ParametrosInmutablesException extends DomainException {

  public ParametrosInmutablesException() {
    super("E_PARAMETROS_INMUTABLES", "Los parametros solo se editan mientras el periodo esta PENDIENTE");
  }
}
