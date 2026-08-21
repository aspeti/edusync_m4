package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el {@link Curso} objetivo no existe o pertenece a un tenant distinto del
 * actor autenticado ({@code DD-UC-010} &sect;2: se devuelve HTTP 404, no 403, mismo criterio
 * que {@code GestionEscolarNoEncontradaException}). Se lanza tanto al consultar un {@link Curso}
 * directamente como al validar el padre antes de crear un {@link Paralelo}.
 */
public class CursoNoEncontradoException extends DomainException {

  public CursoNoEncontradoException() {
    super("E_CURSO_NO_ENCONTRADO", "El curso no existe o no pertenece a este tenant");
  }
}
