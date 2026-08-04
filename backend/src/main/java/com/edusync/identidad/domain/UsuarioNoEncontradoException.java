package com.edusync.identidad.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el usuario objetivo no existe o pertenece a un tenant distinto del actor
 * autenticado (DD-UC-005 &sect;2: se devuelve HTTP 404, no 403, para no confirmar la
 * existencia de un recurso ajeno a otro tenant).
 */
public class UsuarioNoEncontradoException extends DomainException {

  public UsuarioNoEncontradoException() {
    super("E_USUARIO_NO_ENCONTRADO", "El usuario no existe o no pertenece a este tenant");
  }
}
