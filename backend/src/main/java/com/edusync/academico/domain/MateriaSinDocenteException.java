package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Hay materias con curso asignado y sin profesor ({@code FSD-UC-009}).
 * HTTP 409 {@code E_MATERIA_SIN_DOCENTE}. No incluye nombres de personas.
 */
public class MateriaSinDocenteException extends DomainException {

  public MateriaSinDocenteException() {
    super("E_MATERIA_SIN_DOCENTE", "Hay materias con curso asignado sin profesor");
  }
}
