package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de un {@link PeriodoEvaluacion}. Interno al modulo {@code academico}. */
public record PeriodoEvaluacionId(UUID valor) {

  public PeriodoEvaluacionId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static PeriodoEvaluacionId nueva() {
    return new PeriodoEvaluacionId(UUID.randomUUID());
  }

  public static PeriodoEvaluacionId de(UUID valor) {
    return new PeriodoEvaluacionId(valor);
  }
}
