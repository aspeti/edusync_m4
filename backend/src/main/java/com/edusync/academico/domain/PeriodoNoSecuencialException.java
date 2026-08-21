package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Apertura de un periodo {@code k} cuando {@code k-1} no esta {@code CERRADO}
 * ({@code FSD-UC-013} A2, {@code BR-017}).
 */
public class PeriodoNoSecuencialException extends DomainException {

  public PeriodoNoSecuencialException() {
    super(
        "E_PERIODO_NO_SECUENCIAL",
        "El periodo no puede abrirse hasta que el anterior este CERRADO");
  }
}
