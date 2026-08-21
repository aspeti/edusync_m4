package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el {@link SeccionEvaluacion} no existe o pertenece a otro tenant
 * ({@code DD-UC-016} &sect;2: HTTP 404, no 403).
 */
public class SeccionNoEncontradaException extends DomainException {

  public SeccionNoEncontradaException() {
    super("E_SECCION_NO_ENCONTRADA", "La seccion de evaluacion no existe o no pertenece a este tenant");
  }
}
