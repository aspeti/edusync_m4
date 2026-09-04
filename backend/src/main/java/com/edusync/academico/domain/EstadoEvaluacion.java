package com.edusync.academico.domain;

/**
 * Estado de una {@link Evaluacion} ({@code FSD-UC-015}): nace {@code ACTIVA}; la baja
 * logica es {@code ANULADA} (sin DELETE fisico, {@code DD-UC-017} &sect;2).
 */
public enum EstadoEvaluacion {
  ACTIVA,
  ANULADA
}
