package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de un {@link SeccionEvaluacion}. Interno al modulo {@code academico}. */
public record SeccionEvaluacionId(UUID valor) {

  public SeccionEvaluacionId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static SeccionEvaluacionId nueva() {
    return new SeccionEvaluacionId(UUID.randomUUID());
  }

  public static SeccionEvaluacionId de(UUID valor) {
    return new SeccionEvaluacionId(valor);
  }
}
