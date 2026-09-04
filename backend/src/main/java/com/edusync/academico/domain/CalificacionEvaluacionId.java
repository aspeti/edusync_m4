package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de una {@link CalificacionEvaluacion} ({@code DD-UC-018}). */
public record CalificacionEvaluacionId(UUID valor) {

  public CalificacionEvaluacionId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static CalificacionEvaluacionId nueva() {
    return new CalificacionEvaluacionId(UUID.randomUUID());
  }

  public static CalificacionEvaluacionId de(UUID valor) {
    return new CalificacionEvaluacionId(valor);
  }
}
