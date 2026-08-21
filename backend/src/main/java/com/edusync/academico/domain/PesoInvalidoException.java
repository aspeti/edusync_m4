package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * {@code nota} de una seccion fuera de {@code (0, 100]} ({@code FSD-UC-014} A1).
 */
public class PesoInvalidoException extends DomainException {

  public PesoInvalidoException() {
    super("E_PESO_INVALIDO", "La nota de la seccion debe estar en (0, 100]");
  }
}
