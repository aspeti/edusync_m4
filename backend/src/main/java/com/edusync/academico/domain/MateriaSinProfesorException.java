package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * A1 de {@code FSD-UC-015} / {@code BR-022}: no se puede crear una {@link Evaluacion}
 * sobre una {@link Materia} sin profesor asignado. HTTP 409 {@code E_MATERIA_SIN_PROFESOR}.
 */
public class MateriaSinProfesorException extends DomainException {

  public MateriaSinProfesorException() {
    super(
        "E_MATERIA_SIN_PROFESOR",
        "No se puede registrar una evaluacion: la materia no tiene profesor asignado");
  }
}
