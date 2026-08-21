package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * POST/PUT/PATCH de secciones cuando algun periodo de la gestion no esta
 * {@code PENDIENTE} (freeze sticky, {@code ADR-0013} &sect;3.1.5, {@code DD-UC-016} &sect;2).
 */
public class SeccionesInmutablesException extends DomainException {

  public SeccionesInmutablesException() {
    super(
        "E_SECCIONES_INMUTABLES",
        "No se pueden modificar las secciones una vez que un periodo dejo de estar PENDIENTE");
  }
}
