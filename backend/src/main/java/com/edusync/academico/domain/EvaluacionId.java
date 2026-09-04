package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de una {@link Evaluacion}. Interno al modulo {@code academico}. */
public record EvaluacionId(UUID valor) {

  public EvaluacionId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static EvaluacionId nueva() {
    return new EvaluacionId(UUID.randomUUID());
  }

  public static EvaluacionId de(UUID valor) {
    return new EvaluacionId(valor);
  }
}
