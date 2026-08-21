package com.edusync.academico.domain;

/**
 * Estado de una {@link Inscripcion} ({@code FSD-UC-020}, {@code DD-UC-013}).
 * Este slice solo persiste {@code ACTIVA}; {@code RETIRADA}/{@code TRANSFERIDA} quedan
 * para un Design Doc de seguimiento.
 */
public enum EstadoInscripcion {
  ACTIVA,
  RETIRADA,
  TRANSFERIDA
}
