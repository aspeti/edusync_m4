package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el {@link Estudiante} objetivo no existe o pertenece a un tenant distinto
 * del actor autenticado ({@code DD-UC-013} &sect;2: HTTP 404, no 403).
 */
public class EstudianteNoEncontradoException extends DomainException {

  public EstudianteNoEncontradoException() {
    super("E_ESTUDIANTE_NO_ENCONTRADO", "El estudiante no existe o no pertenece a este tenant");
  }
}
