package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Valor de {@link CalificacionEvaluacion} fuera de {@code [0, puntajeMaximo]}.
 * HTTP 422 {@code E_RANGO_INVALIDO} ({@code DD-UC-018} / A2 de {@code FSD-UC-015}).
 */
public class RangoCalificacionInvalidoException extends DomainException {

  public RangoCalificacionInvalidoException(String rangoPermitido) {
    super(
        "E_RANGO_INVALIDO",
        "El valor esta fuera del rango permitido " + rangoPermitido);
  }
}
