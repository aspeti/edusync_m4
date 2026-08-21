package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * POST/DELETE/PATCH de datos de periodos cuando algun periodo de la gestion esta
 * {@code ABIERTO} ({@code ADR-0013} &sect;3.1.4, {@code DD-UC-015} &sect;2).
 */
public class PeriodosInmutablesException extends DomainException {

  public PeriodosInmutablesException() {
    super(
        "E_PERIODOS_INMUTABLES",
        "No se pueden modificar los periodos mientras uno esta ABIERTO");
  }
}
