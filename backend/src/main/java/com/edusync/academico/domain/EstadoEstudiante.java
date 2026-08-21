package com.edusync.academico.domain;

/**
 * Estado de un {@link Estudiante} ({@code FSD-UC-020}, {@code DD-UC-013}).
 * Este slice solo persiste {@code ACTIVO}/{@code INACTIVO}; baja formal queda fuera.
 */
public enum EstadoEstudiante {
  ACTIVO,
  INACTIVO
}
