package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando la {@link Materia} objetivo no existe o pertenece a un tenant distinto
 * del actor autenticado ({@code DD-UC-012} &sect;2: HTTP 404, no 403).
 */
public class MateriaNoEncontradaException extends DomainException {

  public MateriaNoEncontradaException() {
    super("E_MATERIA_NO_ENCONTRADA", "La materia no existe o no pertenece a este tenant");
  }
}
