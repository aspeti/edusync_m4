package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/** Fechas inclusivas de dos periodos de la misma gestion se solapan ({@code FSD-UC-013} A1). */
public class PeriodosSolapadosException extends DomainException {

  public PeriodosSolapadosException() {
    super("E_PERIODOS_SOLAPADOS", "Las fechas de los periodos no pueden solaparse");
  }
}
