package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * El estudiante no pertenece a la nomina de la materia (inscripcion ACTIVA en
 * gestion × curso/paralelo asignado). HTTP 422 {@code E_ESTUDIANTE_NO_INSCRITO}
 * ({@code DD-UC-018}).
 */
public class EstudianteNoInscritoException extends DomainException {

  public EstudianteNoInscritoException() {
    super(
        "E_ESTUDIANTE_NO_INSCRITO",
        "El estudiante no esta inscrito en un curso/paralelo asignado a la materia");
  }
}
