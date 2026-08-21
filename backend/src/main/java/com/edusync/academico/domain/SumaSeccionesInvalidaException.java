package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Plantilla vacia o suma de {@code nota} distinta de 100.00 ({@code FSD-UC-014} A2,
 * {@code ADR-0013}).
 */
public class SumaSeccionesInvalidaException extends DomainException {

  public SumaSeccionesInvalidaException() {
    super(
        "E_SUMA_SECCIONES_INVALIDA",
        "La suma de nota de las secciones de la gestion debe ser exactamente 100");
  }
}
