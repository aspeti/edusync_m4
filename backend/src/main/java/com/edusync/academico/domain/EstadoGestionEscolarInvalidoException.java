package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando se solicita una transicion de {@link EstadoGestionEscolar} no permitida
 * ({@code DD-UC-008} &sect;2): cualquier transicion fuera de {@code PLANIFICACION -> ACTIVA},
 * {@code ACTIVA -> CERRADA} y {@code ACTIVA -> PLANIFICACION}. {@code CERRADA} es terminal.
 */
public class EstadoGestionEscolarInvalidoException extends DomainException {

  public EstadoGestionEscolarInvalidoException(EstadoGestionEscolar actual, EstadoGestionEscolar solicitado) {
    super(
        "E_ESTADO_INVALIDO",
        "No se puede transicionar de " + actual + " a " + solicitado);
  }
}
