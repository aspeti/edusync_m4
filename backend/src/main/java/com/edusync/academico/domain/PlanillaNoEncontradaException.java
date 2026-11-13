package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Tripleta materia/curso/paralelo inexistente o no asignada al Profesor
 * ({@code DD-UC-033}: HTTP 404 {@code E_PLANILLA_NO_ENCONTRADA}).
 */
public class PlanillaNoEncontradaException extends DomainException {

  public PlanillaNoEncontradaException() {
    super("E_PLANILLA_NO_ENCONTRADA", "La planilla no existe o no está asignada a su usuario");
  }
}
