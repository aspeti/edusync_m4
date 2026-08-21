package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el {@link Paralelo} no existe, pertenece a otro tenant o no corresponde
 * al {@link Curso} indicado ({@code DD-UC-012} &sect;2: HTTP 404).
 */
public class ParaleloNoEncontradoException extends DomainException {

  public ParaleloNoEncontradoException() {
    super("E_PARALELO_NO_ENCONTRADO", "El paralelo no existe, no pertenece a este tenant o no corresponde al curso");
  }
}
