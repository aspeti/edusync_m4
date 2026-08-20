package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando la {@link GestionEscolar} objetivo no existe o pertenece a un tenant
 * distinto del actor autenticado ({@code DD-UC-008} &sect;2: se devuelve HTTP 404, no 403,
 * mismo criterio que {@code identidad.domain.UsuarioNoEncontradoException}).
 */
public class GestionEscolarNoEncontradaException extends DomainException {

  public GestionEscolarNoEncontradaException() {
    super("E_GESTION_ESCOLAR_NO_ENCONTRADA", "La gestion escolar no existe o no pertenece a este tenant");
  }
}
