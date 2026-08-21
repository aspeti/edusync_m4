package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el {@code profesorId} no es un {@code Usuario} activo del tenant con rol
 * {@code PROFESOR} ({@code DD-UC-012} &sect;2: HTTP 404).
 */
public class ProfesorNoEncontradoException extends DomainException {

  public ProfesorNoEncontradoException() {
    super("E_PROFESOR_NO_ENCONTRADO", "El profesor no existe, no esta activo o no pertenece a este tenant");
  }
}
