package com.edusync.identidad.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando se intenta construir un {@link Usuario} que viola la invariante
 * permanente {@code tenant_id IS NULL <=> roles == {SYSADMIN}} (ADR-0010).
 */
public class InvarianteRolException extends DomainException {

  public InvarianteRolException(String message) {
    super("E_INVARIANTE_ROL_VIOLADA", message);
  }
}
