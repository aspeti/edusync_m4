package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de un {@link ParametroPeriodo}. Interno al modulo {@code academico}. */
public record ParametroPeriodoId(UUID valor) {

  public ParametroPeriodoId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static ParametroPeriodoId nueva() {
    return new ParametroPeriodoId(UUID.randomUUID());
  }

  public static ParametroPeriodoId de(UUID valor) {
    return new ParametroPeriodoId(valor);
  }
}
