package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * A1 de {@code FSD-UC-020}: segundo intento de inscribir al mismo {@link Estudiante} en la
 * misma {@link GestionEscolar}. HTTP 409 {@code E_INSCRIPCION_DUPLICADA}.
 */
public class InscripcionDuplicadaException extends DomainException {

  public InscripcionDuplicadaException() {
    super(
        "E_INSCRIPCION_DUPLICADA",
        "El estudiante ya esta inscrito en esa Gestion Escolar");
  }
}
