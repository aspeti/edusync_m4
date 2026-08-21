package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el {@link PeriodoEvaluacion} no existe o pertenece a otro tenant
 * ({@code DD-UC-015} &sect;2: HTTP 404, no 403).
 */
public class PeriodoNoEncontradoException extends DomainException {

  public PeriodoNoEncontradoException() {
    super("E_PERIODO_NO_ENCONTRADO", "El periodo de evaluacion no existe o no pertenece a este tenant");
  }
}
