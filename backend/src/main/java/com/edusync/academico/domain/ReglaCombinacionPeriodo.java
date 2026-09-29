package com.edusync.academico.domain;

/**
 * Regla de combinacion persistida en {@link ParametroPeriodo} ({@code DD-UC-029}).
 * El motor vigente ({@code FSD-UC-016}) solo implementa promedio simple; las demas
 * reglas del FSD clasico se rechazan antes de construir el aggregate.
 */
public enum ReglaCombinacionPeriodo {
  PROMEDIO_SIMPLE;

  public static ReglaCombinacionPeriodo parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new ReglaCombinacionNoSoportadaException();
    }
    try {
      return valueOf(raw.trim());
    } catch (IllegalArgumentException ex) {
      throw new ReglaCombinacionNoSoportadaException();
    }
  }
}
