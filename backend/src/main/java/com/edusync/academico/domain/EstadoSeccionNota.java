package com.edusync.academico.domain;

/**
 * Estado de la nota de una seccion en la vista provisional ({@code DD-UC-018},
 * {@code ADR-0013} §3.4). {@code INCOMPLETO} = ninguna evaluacion ACTIVA con nota.
 */
public enum EstadoSeccionNota {
  COMPLETO,
  INCOMPLETO
}
