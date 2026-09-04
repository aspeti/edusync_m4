package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Escritura de {@link Evaluacion} cuando el {@link PeriodoEvaluacion} no esta
 * {@code ABIERTO}. HTTP 422 {@code E_PERIODO_NO_ABIERTO} ({@code DD-UC-017} &sect;2).
 */
public class PeriodoNoAbiertoException extends DomainException {

  public PeriodoNoAbiertoException() {
    super("E_PERIODO_NO_ABIERTO", "Solo se pueden modificar evaluaciones de un periodo ABIERTO");
  }
}
