package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * A1 de {@code FSD-UC-018}: se intenta asignar un profesor a una materia sin una
 * {@link AsignacionMateriaCurso} previa para el mismo {@code (cursoId, paraleloId)}.
 * HTTP 409 {@code E_MATERIA_SIN_CURSO}.
 */
public class MateriaSinCursoException extends DomainException {

  public MateriaSinCursoException() {
    super(
        "E_MATERIA_SIN_CURSO",
        "No se puede asignar un profesor: la materia no tiene ese curso/paralelo asignado");
  }
}
