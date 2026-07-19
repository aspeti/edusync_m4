package com.edusync.plataforma.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza al registrar un {@link Tenant} sin {@code fechaVencimientoSuscripcion}
 * ({@code FSD-UC-011}, flujo alternativo A1).
 */
public class SuscripcionIncompletaException extends DomainException {

  public SuscripcionIncompletaException() {
    super("E_SUSCRIPCION_INCOMPLETA", "fechaVencimientoSuscripcion es obligatoria para registrar un Tenant");
  }
}
