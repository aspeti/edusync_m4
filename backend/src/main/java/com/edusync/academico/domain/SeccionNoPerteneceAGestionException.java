package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * La {@link SeccionEvaluacion} no es de la misma {@link GestionEscolar} que el
 * {@link PeriodoEvaluacion}. HTTP 422 {@code E_SECCION_NO_PERTENECE_A_GESTION}.
 */
public class SeccionNoPerteneceAGestionException extends DomainException {

  public SeccionNoPerteneceAGestionException() {
    super(
        "E_SECCION_NO_PERTENECE_A_GESTION",
        "La seccion no pertenece a la misma gestion escolar que el periodo");
  }
}
