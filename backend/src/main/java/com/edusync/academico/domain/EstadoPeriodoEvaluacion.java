package com.edusync.academico.domain;

/**
 * Ciclo de un {@link PeriodoEvaluacion} ({@code FSD-UC-013}, {@code ADR-0013}):
 * {@code PENDIENTE -> ABIERTO -> CERRADO}. No hay reapertura de {@code CERRADO} en este slice.
 */
public enum EstadoPeriodoEvaluacion {
  PENDIENTE,
  ABIERTO,
  CERRADO
}
