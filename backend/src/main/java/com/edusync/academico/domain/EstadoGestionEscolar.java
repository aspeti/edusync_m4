package com.edusync.academico.domain;

/**
 * Ciclo de vida de una {@link GestionEscolar} ({@code FSD-UC-012}, {@code DD-UC-008}).
 *
 * <p>Transiciones validas: {@code PLANIFICACION -> ACTIVA}, {@code ACTIVA -> CERRADA},
 * {@code ACTIVA -> PLANIFICACION} (reabrir planificacion). {@code CERRADA} es terminal en
 * este slice: no existe reapertura de una gestion cerrada.
 */
public enum EstadoGestionEscolar {
  PLANIFICACION,
  ACTIVA,
  CERRADA
}
