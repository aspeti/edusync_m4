package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza al crear una {@link GestionEscolar} cuya {@code fechaFin} no es posterior a
 * {@code fechaInicio} ({@code FSD-UC-012}, flujo alternativo A1).
 */
public class FechasInvalidasException extends DomainException {

  public FechasInvalidasException() {
    super("E_FECHAS_INVALIDAS", "fechaFin debe ser posterior a fechaInicio");
  }
}
